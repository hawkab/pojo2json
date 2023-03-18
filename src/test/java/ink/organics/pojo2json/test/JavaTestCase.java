package ink.organics.pojo2json.test;


import ink.organics.pojo2json.EditorPopupMenuDefaultAction;


public class JavaTestCase extends TestCase {


    @Override
    protected String getTestDataPath() {
        return "src/test/java/testdata/java";
    }


    public void testCurrent() {
        dataTypeTestModel.testSpecialObjectTestPOJO("SpecialObjectTestPOJO.java", new EditorPopupMenuDefaultAction());
    }

    public void testJavaVariable() {
        variableTestModel.testVariableTestPOJO("VariableTestPOJO.java", new EditorPopupMenuDefaultAction());
    }

    public void testJavaDateType() {
        dataTypeTestModel.testPrimitiveTestPOJO("PrimitiveTestPOJO.java", new EditorPopupMenuDefaultAction());
        dataTypeTestModel.testPrimitiveArrayTestPOJO("PrimitiveArrayTestPOJO.java", new EditorPopupMenuDefaultAction());
        dataTypeTestModel.testEnumTestPOJO("EnumTestPOJO.java", new EditorPopupMenuDefaultAction());
        dataTypeTestModel.testIterableTestPOJO("IterableTestPOJO.java", new EditorPopupMenuDefaultAction());
        dataTypeTestModel.testGenericTestPOJO("GenericTestPOJO.java", new EditorPopupMenuDefaultAction());
        dataTypeTestModel.testSpecialObjectTestPOJO("SpecialObjectTestPOJO.java", new EditorPopupMenuDefaultAction());
    }


    public void testJavaAnnotation() {
        annotationTestModel.testJsonPropertyTestPOJO("JsonPropertyTestPOJO.java", new EditorPopupMenuDefaultAction());
        annotationTestModel.testJsonIgnoreTestPOJO("JsonIgnoreTestPOJO.java", new EditorPopupMenuDefaultAction());
        annotationTestModel.testJsonIgnorePropertiesTestPOJO("JsonIgnorePropertiesTestPOJO.java", new EditorPopupMenuDefaultAction());
        annotationTestModel.testJsonIgnoreTypeTestPOJO("JsonIgnoreTypeTestPOJO.java", new EditorPopupMenuDefaultAction());
    }


    public void testJavaDoc() {
        docTestModel.testJsonIgnoreDocTestPOJO("JsonIgnoreDocTestPOJO.java", new EditorPopupMenuDefaultAction());
        docTestModel.testJsonIgnorePropertiesDocTestPOJO("JsonIgnorePropertiesDocTestPOJO.java", new EditorPopupMenuDefaultAction());
    }


    public void testJavaStaticField() {
        staticFieldTestModel.testStaticFieldPOJO("StaticFieldPOJO.java", new EditorPopupMenuDefaultAction());
    }


    public void testJavaMemberClass() {
        memberClassTestModel.testMemberClassTestPOJO("MemberClassTestPOJO.java", new EditorPopupMenuDefaultAction());
    }
}
