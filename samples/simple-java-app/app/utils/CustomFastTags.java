package utils;

import play.template2.GTContentRenderer;
import play.template2.GTFastTag;
import play.template2.GTJavaBase;

import java.util.Map;

public class CustomFastTags extends GTFastTag {

    public static void tag_testFastTag(GTJavaBase template, Map<String, Object> args, GTContentRenderer content ) {
        template.out.append("[testFastTag before]x");
//        if ( 1 == 1) throw new RuntimeException("test error");
        template.insertOutput( content.render());
        template.out.append("[from testFastTag after]");
    }

}
