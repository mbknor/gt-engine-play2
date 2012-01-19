package kjetland.gtengineplay;

import org.apache.commons.lang.StringUtils;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.Http;
import play.template2.GTContentRenderer;
import play.template2.GTFastTag;
import play.template2.GTJavaBase;
import play.template2.compile.GTInternalFastTags;
import play.template2.exceptions.GTTemplateRuntimeException;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GTFastTagsInternal2impl extends GTFastTag {

    public static void tag_form(GTJavaBase template, Map<String, Object> args, GTContentRenderer content) {
        String url = (String) args.get("arg");
        if (url == null) {
            url = (String) args.get("action");
        }
        if (url == null) {
            throw new GTTemplateRuntimeException("Missing action/url");
        }
        String enctype = (String) args.get("enctype");
        if (enctype == null) {
            enctype = "application/x-www-form-urlencoded";
        }
        String method = "POST"; // prefer POST for form ....
        
        if (args.containsKey("method")) {
            method = args.get("method").toString();
        }
        if (!("GET".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method))) {
            String separator = url.indexOf('?') != -1 ? "&" : "?";
            url += separator + "x-http-method-override=" + method.toUpperCase();
            method = "POST";
        }
        String encoding = "utf-8";
        StringWriter out = template.out;
        out.append("<form action=\"" + url + "\" method=\"" + method.toLowerCase() + "\" accept-charset=\"" + encoding + "\" enctype=\"" + enctype + "\" " + GTInternalFastTags.serialize(args, "action", "method", "accept-charset", "enctype") + ">");
        if (!("GET".equalsIgnoreCase(method))) {
            //_authenticityToken(args, body, out, template, fromLine);
        }
        template.insertOutput(content.render());
        out.append("</form>");
    }

    public static void tag_a(GTJavaBase template, Map<String, Object> args, GTContentRenderer content) {
        String url = (String) args.get("arg");
        if (url == null) {
            url = (String) args.get("action");
        }
        if (url == null) {
            throw new GTTemplateRuntimeException("Missing action/url");
        }
        StringWriter out = template.out;
        out.append("<a href=\"" + url + "\" " + GTInternalFastTags.serialize(args, "href") + ">");
        template.insertOutput(content.render());
        out.append("</a>");
    }

    public static void tag_error(GTJavaBase template, Map<String, Object> args, GTContentRenderer content) {
        if (args.get("arg") == null && args.get("key") == null) {
            throw new GTTemplateRuntimeException("Please specify the error key");
        }
        String key = args.get("arg") == null ? args.get("key") + "" : args.get("arg") + "";
        
        GTJavaBase2xImpl t = (GTJavaBase2xImpl)template;
        
        
        if (t.form().isEmpty()) {
            throw new GTTemplateRuntimeException("Cannot find form - cannot find errors");
        }
        
        Form form = t.form().get();
        
        List<ValidationError> error = (List<ValidationError>)form.errors().get(key);

        if ( error == null || error.isEmpty()) {
            return ;
        }

        String errorMessage = error.get(0).message();

        template.out.append(errorMessage);

    }



}
