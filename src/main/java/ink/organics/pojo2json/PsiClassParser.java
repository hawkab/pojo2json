package ink.organics.pojo2json;

import com.google.gson.GsonBuilder;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.util.PsiUtil;
import ink.organics.pojo2json.fake.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class PsiClassParser {


    private final GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();

    private final Map<String, JsonFakeValuesService> normalTypes = new HashMap<>();

    private final List<String> iterableTypes = List.of(
            "Iterable",
            "Collection",
            "List",
            "Set");

    public PsiClassParser() {

        FakeDecimal fakeDecimal = new FakeDecimal();
        FakeLocalDateTime fakeLocalDateTime = new FakeLocalDateTime();

        normalTypes.put("Boolean", new FakeBoolean());
        normalTypes.put("Float", fakeDecimal);
        normalTypes.put("Double", fakeDecimal);
        normalTypes.put("BigDecimal", fakeDecimal);
        normalTypes.put("Number", new FakeInteger());
        normalTypes.put("Character", new FakeChar());
        normalTypes.put("CharSequence", new FakeString());
        normalTypes.put("Date", fakeLocalDateTime);
        normalTypes.put("Temporal", new FakeTemporal());
        normalTypes.put("LocalDateTime", fakeLocalDateTime);
        normalTypes.put("LocalDate", new FakeLocalDate());
        normalTypes.put("LocalTime", new FakeLocalTime());
        normalTypes.put("ZonedDateTime", new FakeZonedDateTime());
        normalTypes.put("YearMonth", new FakeYearMonth());
        normalTypes.put("UUID", new FakeUUID());
    }

    public String psiClassToJSONString(PsiClass psiClass) {
        Map<String, Object> kv = parseClass(psiClass, 0, List.of());
        return gsonBuilder.create().toJson(kv);
    }

    private Map<String, Object> parseClass(PsiClass psiClass, int level, List<String> ignoreProperties) {
        PsiAnnotation annotation = psiClass.getAnnotation(com.fasterxml.jackson.annotation.JsonIgnoreType.class.getName());
        if (annotation != null) {
            return null;
        }
        return Arrays.stream(psiClass.getAllFields())
                .map(field -> parseField(field, level, ignoreProperties))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (ov, nv) -> ov, LinkedHashMap::new));
    }


    private Map.Entry<String, Object> parseField(PsiField field, int level, List<String> ignoreProperties) {
        // 移除所有 static 属性，这其中包括 kotlin 中的 companion object 和 INSTANCE
        if (field.hasModifierProperty(PsiModifier.STATIC)) {
            return null;
        }

        if (ignoreProperties.contains(field.getName())) {
            return null;
        }

        PsiAnnotation annotation = field.getAnnotation(com.fasterxml.jackson.annotation.JsonIgnore.class.getName());
        if (annotation != null) {
            return null;
        }

        PsiDocComment docComment = field.getDocComment();
        if (docComment != null) {
            PsiDocTag psiDocTag = docComment.findTagByName("JsonIgnore");
            if (psiDocTag != null && "JsonIgnore".equals(psiDocTag.getName())) {
                return null;
            }

            ignoreProperties = POJO2JsonPsiUtils.docTextToList("@JsonIgnoreProperties", docComment.getText());
        } else {
            annotation = field.getAnnotation(com.fasterxml.jackson.annotation.JsonIgnoreProperties.class.getName());
            if (annotation != null) {
                ignoreProperties = POJO2JsonPsiUtils.arrayTextToList(annotation.findAttributeValue("value").getText());
            }
        }

        String fieldKey = parseFieldKey(field);
        if (fieldKey == null) {
            return null;
        }
        Object fieldValue = parseFieldValue(field, level, ignoreProperties);
        if (fieldValue == null) {
            return null;
        }
        return Map.entry(fieldKey, fieldValue);
    }

    private String parseFieldKey(PsiField field) {

        PsiAnnotation annotation = field.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class.getName());
        if (annotation != null) {
            String fieldName = POJO2JsonPsiUtils.psiTextToString(annotation.findAttributeValue("value").getText());
            if (StringUtils.isNotBlank(fieldName)) {
                return fieldName;
            }
        }

        annotation = field.getAnnotation("com.alibaba.fastjson.annotation.JSONField");
        if (annotation != null) {
            String fieldName = POJO2JsonPsiUtils.psiTextToString(annotation.findAttributeValue("name").getText());
            if (StringUtils.isNotBlank(fieldName)) {
                return fieldName;
            }
        }
        return field.getName();
    }

    private Object parseFieldValue(PsiField field, int level, List<String> ignoreProperties) {
        return parseFieldValueType(field.getType(), level, ignoreProperties);
    }

    private Object parseFieldValueType(PsiType type, int level, List<String> ignoreProperties) {

        level = ++level;

        if (type instanceof PsiPrimitiveType) {       //primitive Type

            return getPrimitiveTypeValue(type);

        } else if (type instanceof PsiArrayType) {   //array type

            PsiType deepType = type.getDeepComponentType();
            Object obj = parseFieldValueType(deepType, level, ignoreProperties);
            return obj != null ? List.of(obj) : List.of();

        } else {    //reference Type

            PsiClass psiClass = PsiUtil.resolveClassInClassTypeOnly(type);

            if (psiClass == null) {
                return new LinkedHashMap<>();
            }

            if (psiClass.isEnum()) { // enum

                for (PsiField field : psiClass.getFields()) {
                    if (field instanceof PsiEnumConstant) {
                        return field.getName();
                    }
                }
                return "";

            } else {

                List<String> fieldTypeNames = new ArrayList<>();

                fieldTypeNames.add(type.getPresentableText());
                fieldTypeNames.addAll(Arrays.stream(type.getSuperTypes())
                        .map(PsiType::getPresentableText).collect(Collectors.toList()));


                boolean iterable = fieldTypeNames.stream().map(typeName -> {
                    int subEnd = typeName.indexOf("<");
                    return typeName.substring(0, subEnd > 0 ? subEnd : typeName.length());
                }).anyMatch(iterableTypes::contains);

                if (iterable) {// Iterable

                    PsiType deepType = PsiUtil.extractIterableTypeParameter(type, false);
                    Object obj = parseFieldValueType(deepType, level, ignoreProperties);
                    return obj != null ? List.of(obj) : List.of();

                } else { // Object

                    List<String> retain = new ArrayList<>(fieldTypeNames);
                    retain.retainAll(normalTypes.keySet());
                    if (!retain.isEmpty()) {
                        return this.getFakeValue(normalTypes.get(retain.get(0)));
                    } else {

                        if (level > 500) {
                            throw new KnownException("This class reference level exceeds maximum limit or has nested references!");
                        }

                        return parseClass(psiClass, level, ignoreProperties);
                    }
                }
            }
        }
    }

    private Object getPrimitiveTypeValue(PsiType type) {
        switch (type.getCanonicalText()) {
            case "boolean":
                return this.getFakeValue(normalTypes.get("Boolean"));
            case "byte":
            case "short":
            case "int":
            case "long":
                return this.getFakeValue(normalTypes.get("Number"));
            case "float":
            case "double":
                return this.getFakeValue(normalTypes.get("BigDecimal"));
            case "char":
                return this.getFakeValue(normalTypes.get("Character"));
            default:
                return null;
        }
    }

}