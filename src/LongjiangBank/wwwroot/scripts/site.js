; (function (f) { "use strict"; "function" === typeof define && define.amd ? define(["jquery"], f) : "undefined" !== typeof module && module.exports ? module.exports = f(require("jquery")) : f(jQuery) })(function ($) { "use strict"; function n(a) { return !a.nodeName || -1 !== $.inArray(a.nodeName.toLowerCase(), ["iframe", "#document", "html", "body"]) } function h(a) { return $.isFunction(a) || $.isPlainObject(a) ? a : { top: a, left: a } } var p = $.scrollTo = function (a, d, b) { return $(window).scrollTo(a, d, b) }; p.defaults = { axis: "xy", duration: 0, limit: !0 }; $.fn.scrollTo = function (a, d, b) { "object" === typeof d && (b = d, d = 0); "function" === typeof b && (b = { onAfter: b }); "max" === a && (a = 9E9); b = $.extend({}, p.defaults, b); d = d || b.duration; var u = b.queue && 1 < b.axis.length; u && (d /= 2); b.offset = h(b.offset); b.over = h(b.over); return this.each(function () { function k(a) { var k = $.extend({}, b, { queue: !0, duration: d, complete: a && function () { a.call(q, e, b) } }); r.animate(f, k) } if (null !== a) { var l = n(this), q = l ? this.contentWindow || window : this, r = $(q), e = a, f = {}, t; switch (typeof e) { case "number": case "string": if (/^([+-]=?)?\d+(\.\d+)?(px|%)?$/.test(e)) { e = h(e); break } e = l ? $(e) : $(e, q); if (!e.length) return; case "object": if (e.is || e.style) t = (e = $(e)).offset() } var v = $.isFunction(b.offset) && b.offset(q, e) || b.offset; $.each(b.axis.split(""), function (a, c) { var d = "x" === c ? "Left" : "Top", m = d.toLowerCase(), g = "scroll" + d, h = r[g](), n = p.max(q, c); t ? (f[g] = t[m] + (l ? 0 : h - r.offset()[m]), b.margin && (f[g] -= parseInt(e.css("margin" + d), 10) || 0, f[g] -= parseInt(e.css("border" + d + "Width"), 10) || 0), f[g] += v[m] || 0, b.over[m] && (f[g] += e["x" === c ? "width" : "height"]() * b.over[m])) : (d = e[m], f[g] = d.slice && "%" === d.slice(-1) ? parseFloat(d) / 100 * n : d); b.limit && /^\d+$/.test(f[g]) && (f[g] = 0 >= f[g] ? 0 : Math.min(f[g], n)); !a && 1 < b.axis.length && (h === f[g] ? f = {} : u && (k(b.onAfterFirst), f = {})) }); k(b.onAfter) } }) }; p.max = function (a, d) { var b = "x" === d ? "Width" : "Height", h = "scroll" + b; if (!n(a)) return a[h] - $(a)[b.toLowerCase()](); var b = "client" + b, k = a.ownerDocument || a.document, l = k.documentElement, k = k.body; return Math.max(l[h], k[h]) - Math.min(l[b], k[b]) }; $.Tween.propHooks.scrollLeft = $.Tween.propHooks.scrollTop = { get: function (a) { return $(a.elem)[a.prop]() }, set: function (a) { var d = this.get(a); if (a.options.interrupt && a._last && a._last !== d) return $(a.elem).stop(); var b = Math.round(a.now); d !== b && ($(a.elem)[a.prop](b), a._last = this.get(a)) } }; return p });

$(document).ready(function () {
    $('.datetime').datetimepicker();
});

function postDelete(url, id) {
    $.post(url, { '__RequestVerificationToken': csrf }, function (data) {
        $('#' + id).remove();
        if (data == 'ok' || data == 'OK')
            ;
        else
            popMsg(data);
        closeDialog();
    });
}

function deleteDialog(url, id) {
    var html = '<div class="message-bg"></div><div class="message-outer"><div class="message-container">' +
        '<h3>提示信息</h3>' +
        '<p>点击删除按钮后，该记录将被永久删除，您确定要这样做吗？</p>' +
        '<div class="message-buttons"><a href="javascript:postDelete(\'' + url + '\', \'' + id + '\')" class="btn red">删除</a> <a href="javascript:closeDialog()" class="btn">取消</a></div>' +
        '</div></div>';
    var dom = $(html);
    $('body').append(dom);
    $(".message-outer").css('margin-top', -($(".message-outer").outerHeight() / 2));
    setTimeout(function () { $(".message-bg").addClass('active'); $(".message-outer").addClass('active'); }, 10);
}

function closeDialog() {
    $('.message-bg').removeClass('active');
    $('.message-outer').removeClass('active');
    setTimeout(function () {
        $('.message-bg').remove();
        $('.message-outer').remove();
    }, 200);
}

$(document).ready(function () {

    $('input[type="text"]').focus(function () {
        $(this).removeClass('error');
    });

    $("form").submit(function (e) {
        if ($(this).hasClass("search")) return;
        $.each($(this).find("input[type='text']"), function (i, item) {
            if ($(item).val() == "" && $(item).attr("name") != "undefined" && !$(item).hasClass("nullable")) {
                $(item).addClass('error');
                $(".wrap-cont").scrollTo($(item), 500);
                e.preventDefault();
                return false;
            }
            else if ($(item).hasClass('chk-number')) {
                var reg = new RegExp("^[0-9]*$");
                if (!reg.test($(item).val())) {
                    $(item).addClass('error');
                    $(".wrap-cont").scrollTo($(item), 500);
                    e.preventDefault();
                    return false;
                }
            }
            else if ($(item).hasClass('chk-price')) {
                var reg = /^([1-9][\d]{0,7}|0)(\.[\d]{1,2})?$/;
                if (!reg.test($(item).val())) {
                    $(item).addClass('error');
                    $(".wrap-cont").scrollTo($(item), 500);
                    e.preventDefault();
                    return false;
                }
            }
            else if ($(item).hasClass('chk-email')) {
                var reg = /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/;
                if (!reg.test($(item).val())) {
                    $(item).addClass('error');
                    $(".wrap-cont").scrollTo($(item), 500);
                    e.preventDefault();
                    return false;
                }
            }
            else if ($(item).hasClass('chk-integer')) {
                var reg = new RegExp(new RegExp("^[0-9]+$"));
                if (!reg.test($(item).val())) {
                    $(item).addClass('error');
                    $(".wrap-cont").scrollTo($(item), 500);
                    e.preventDefault();
                    return false;
                }
            }
            else {
                return true;
            }
        });
    });
});