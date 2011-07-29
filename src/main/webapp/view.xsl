<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/djatokaViewer">
		<xsl:variable name="path" select="defaultPath" />

		<html>
			<head>
				<link rel="stylesheet" type="text/css" href="/view.css" />
				<title>Image Navigator</title>
			</head>
			<body>
				<div id="header">Image Navigator</div>
				<div id="totals">
					<span class="bold">TIFFs: </span>
					<xsl:value-of select="tifStats/@fileCount" />
					files
					<xsl:text>(</xsl:text>
					<xsl:value-of select="tifStats/@totalSize" />
					<xsl:text>)</xsl:text>
					~
					<span class="bold">JP2s: </span>
					<xsl:value-of select="jp2Stats/@fileCount" />
					files
					<xsl:text>(</xsl:text>
					<xsl:value-of select="jp2Stats/@totalSize" />
					<xsl:text>)</xsl:text>
				</div>
				<div id="breadcrumbs">
					Path: /
					<a href="{concat($path, '/')}">ROOT</a>
					<xsl:for-each select="path/part">
						<text> / </text>
						<xsl:value-of select="." />
					</xsl:for-each>
				</div>
				<div id="folder">
					<xsl:for-each select="dir">
						<div class="folder">
							<a href="{concat(./@name, '/')}" title="{./@name}">
							<img src="/images/folder.png" width="75%" alt="{./@name}"/>
							<xsl:choose>
								<xsl:when test="string-length(./@name) &gt; 11">
									<xsl:value-of select="concat(substring(./@name, 0, 11), '...')"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="./@name"/>
								</xsl:otherwise>
							</xsl:choose>
							</a>
						</div>
					</xsl:for-each>
				</div>
				<div id="image">
					<div class="image">asdf</div>
					<div class="image">asdf</div>
					<div class="image">asdf</div>
					<div class="image">asdf</div>
					<div class="image">asdf</div>
					<div class="image">asdf</div>
					<div class="image">asdf</div>
					<div class="image">asdf</div>
					<div class="image">asdf</div>
					<div class="image">asdf</div>
				</div>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>