<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

--%>

<%@page language="java"%><%
    response.setContentType("text/x-component");
	response.setHeader("Cache-Control", "public, max-age=604800, post-check=7200, pre-check=604800");
	response.setDateHeader("Expires", System.currentTimeMillis() + 604800 * 1000); // 7 days
    final String path = request.getContextPath() + "/css/blank.gif";
%>
<public:component lightWeight="true">
<public:attach event="onpropertychange" onevent="propertyChanged()" />
<public:attach event="onbeforeprint" onevent="beforePrint()" for="window"/>
<public:attach event="onafterprint" onevent="afterPrint()" for="window"/>
<script>

/*
 * PNG Behavior
 *
 * This script was created by Erik Arvidsson (http://webfx.eae.net/contact.html#erik)
 * for WebFX (http://webfx.eae.net)
 * Copyright 2002-2004
 *
 * For usage see license at http://webfx.eae.net/license.html
 *
 * Version: 1.02
 * Created: 2001-??-??	First working version
 * Updated: 2002-03-28	Fixed issue when starting with a non png image and
 *                      switching between non png images
 *          2003-01-06	Fixed RegExp to correctly work with IE 5.0x
 *          2004-05-09  When printing revert to original
 *
 */

var supported = /MSIE ((5\.5)|6)/.test(navigator.userAgent) &&
				(navigator.platform == "Win32" || navigator.platform == "Win64");

var realSrc;
var blankSrc = "<%=path%>";
var isPrinting = false;

if (supported) fixImage();

function propertyChanged() {
	if (!supported || isPrinting) return;

	var pName = event.propertyName;
	if (pName != "src") return;
	// if not set to blank
	if (!new RegExp(blankSrc).test(src))
		fixImage();
};

function fixImage() {
	// get src
	var src = element.src;

	// check for real change
	if (src == realSrc && /\.png$/i.test(src)) {
		element.src = blankSrc;
		return;
	}

	if ( ! new RegExp(blankSrc).test(src)) {
		// backup old src
		realSrc = src;
	}

	// test for png
	if (/\.png$/i.test(realSrc)) {
		// set blank image
		element.src = blankSrc;
		// set filter
		element.runtimeStyle.filter = "progid:DXImageTransform.Microsoft." +
					"AlphaImageLoader(src='" + src + "',sizingMethod='scale')";
	}
	else {
		// remove filter
		element.runtimeStyle.filter = "";
	}
}

function beforePrint() {
	isPrinting = true;
	element.src = realSrc;
	element.runtimeStyle.filter = "";
	realSrc = null;
}

function afterPrint() {
	isPrinting = false;
	fixImage();
}

</script>
</public:component>
