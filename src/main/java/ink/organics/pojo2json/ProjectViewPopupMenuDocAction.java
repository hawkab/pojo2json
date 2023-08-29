package ink.organics.pojo2json;

import ink.organics.pojo2json.parser.POJO2JSONParserFactory;

public class ProjectViewPopupMenuDocAction extends ProjectViewPopupMenuAction {

    public ProjectViewPopupMenuDocAction() {
        super(POJO2JSONParserFactory.DEFAULT_POJO_2_JSON_PARSER);
    }

    @Override
    public boolean isNeedJavaDoc() {
        return true;
    }
}
