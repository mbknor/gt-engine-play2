*{
 *  create a link element for a CSS file under /public/stylesheets
 *  src (required) filename without the leading path "/public/stylesheets"
 *  id (optional) an id attribute for the generated link tag
 *  media (optional) media : screen, print, aural, projection ...
 *  title (optional) title atttribute (or description)
 *    ${stylesheet src:'default.css' media:'screen,print' /}
}*
%{
    (_arg ) && (_src = _arg);

    if (!_src) {
        throw new play.template2.exceptions.GTTemplateRuntimeException("src attribute cannot be empty for stylesheet tag");
    }
    _src = "/assets/stylesheets/" + _src
    try {
        _abs = controllers.routes.Assets.at(\"/public/stylesheets/" + _src + "\").url();
    } catch (Exception ex) {
        throw new play.template2.exceptions.GTTemplateRuntimeException("File not found: " + _src);
    }}%
<link rel="stylesheet" type="text/css"#{if _id} id="${_id}"#{/if}#{if _title} title="${_title}"#{/if} href="${_abs}"#{if _media} media="${_media}"#{/if} charset="${_response_encoding}" ></link>
