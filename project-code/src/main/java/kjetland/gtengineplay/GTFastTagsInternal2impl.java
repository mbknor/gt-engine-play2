package kjetland.gtengineplay;

import org.apache.commons.lang.StringUtils;
import play.api.mvc.Call;
import play.api.mvc.Request;
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
        Call call = (Call) args.get("arg");
        if (call == null) {
            call = (Call) args.get("action");
        }
        if (call == null) {
            throw new GTTemplateRuntimeException("Missing action/url");
        }
        String enctype = (String) args.get("enctype");
        if (enctype == null) {
            enctype = "application/x-www-form-urlencoded";
        }
        String method = call.method();
        
        if (args.containsKey("method")) {
            method = args.get("method").toString();
        }
        
        String url = null;
        
        if ( args.containsKey("_use_absoluteURL")) {
            url = call.absoluteURL(Http.Context.current().request());
        } else {
            url = call.url();
        }
        
        if (!("GET".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method))) {
            String separator = url.indexOf('?') != -1 ? "&" : "?";
            url += separator + "x-http-method-override=" + method.toUpperCase();
            method = "POST";
        }
        String encoding = "utf-8";
        template.out.append("<form action=\"" + url + "\" method=\"" + method.toLowerCase() + "\" accept-charset=\"" + encoding + "\" enctype=\"" + enctype + "\" " + GTInternalFastTags.serialize(args, "action", "method", "accept-charset", "enctype") + ">");
        if (!("GET".equalsIgnoreCase(method))) {
            //_authenticityToken(args, body, out, template, fromLine);
        }
        template.insertOutput(content.render());
        template.out.append("</form>");
    }

    public static void tag_a(GTJavaBase template, Map<String, Object> args, GTContentRenderer content) {
        Call call = (Call) args.get("arg");
        if (call == null) {
            call = (Call) args.get("action");
        }
        if (call == null) {
            throw new GTTemplateRuntimeException("Missing action/url");
        }
        
        String url = null;
        if ( args.containsKey("_use_absoluteURL")) {
            url = call.absoluteURL(Http.Context.current().request());
        } else {
            url = call.url();
        }

        template.out.append("<a href=\"" + url + "\" " + GTInternalFastTags.serialize(args, "href") + ">");
        template.insertOutput(content.render());
        template.out.append("</a>");
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
