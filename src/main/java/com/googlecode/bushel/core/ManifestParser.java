/*
 * Copyright 2008-2009 Bushel Project Members (http://bushel.googlecode.com/)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.bushel.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import com.googlecode.bushel.util.Version;
import com.googlecode.bushel.util.VersionRange;

/**
 * Provides an OSGi manifest parser.
 * 
 */
public class ManifestParser {

    private static final String EXPORT_PACKAGE = "Export-Package";
    private static final String IMPORT_PACKAGE = "Import-Package";
    private static final String EXPORT_SERVICE = "Export-Service";
    private static final String IMPORT_SERVICE = "Import-Service";
    private static final String REQUIRE_BUNDLE = "Require-Bundle";
    private static final String BUNDLE_VERSION = "Bundle-Version";
    private static final String BUNDLE_NAME = "Bundle-Name";
    private static final String BUNDLE_DESCRIPTION = "Bundle-Description";
    private static final String BUNDLE_SYMBOLIC_NAME = "Bundle-SymbolicName";
    private static final String BUNDLE_MANIFEST_VERSION = "Bundle-ManifestVersion";
    private static final String BUNDLE_REQUIRED_EXECUTION_ENVIRONMENT = "Bundle-RequiredExecutionEnvironment";
    private static final String BUNDLE_CLASSPATH = "Bundle-ClassPath";

    private static final String ATTR_RESOLUTION = "resolution";
    private static final String ATTR_VERSION = "version";
    private static final String ATTR_BUNDLE_VERSION = "bundle-version";
    private static final String ATTR_USE = "use";

    public static BundleInfo parseJarManifest(InputStream jarStream) throws IOException, ParseException {
        final JarInputStream jis = new JarInputStream(jarStream);
        final BundleInfo parseManifest = parseManifest(jis.getManifest());
        jis.close();
        return parseManifest;
    }

    public static BundleInfo parseManifest(File manifestFile) throws IOException, ParseException {
        final FileInputStream fis = new FileInputStream(manifestFile);
        final BundleInfo parseManifest = parseManifest(fis);
        fis.close();
        return parseManifest;
    }

    public static BundleInfo parseManifest(InputStream manifestStream) throws IOException, ParseException {
        final BundleInfo parseManifest = parseManifest(new Manifest(manifestStream));
        manifestStream.close();
        return parseManifest;
    }

    public static BundleInfo parseManifest(Manifest manifest) throws ParseException {
        Attributes mainAttributes = manifest.getMainAttributes();

        String manifestVersion = mainAttributes.getValue(BUNDLE_MANIFEST_VERSION);
        if (manifestVersion == null) {
            // non OSGi manifest
            throw new ParseException("No " + BUNDLE_MANIFEST_VERSION + " in the manifest", 0);
        }

        String symbolicName = new ManifestHeaderValue(mainAttributes.getValue(BUNDLE_SYMBOLIC_NAME)).getSingleValue();
        if (symbolicName == null) {
            throw new ParseException("No " + BUNDLE_SYMBOLIC_NAME + " in the manifest", 0);
        }

        String description = new ManifestHeaderValue(mainAttributes.getValue(BUNDLE_DESCRIPTION)).getSingleValue();
        if (description == null) {
            description = new ManifestHeaderValue(mainAttributes.getValue(BUNDLE_DESCRIPTION)).getSingleValue();
        }

        String vBundle = new ManifestHeaderValue(mainAttributes.getValue(BUNDLE_VERSION)).getSingleValue();
        Version version;
        try {
            version = versionOf(vBundle);
        } catch (NumberFormatException e) {
            throw new ParseException("The " + BUNDLE_VERSION + " has an incorrect version: " + vBundle + " ("
                    + e.getMessage() + ")", 0);
        }

        BundleInfo bundleInfo = new BundleInfo(symbolicName, version);

        bundleInfo.setDescription(description);

        List<String> environments = new ManifestHeaderValue(mainAttributes
                .getValue(BUNDLE_REQUIRED_EXECUTION_ENVIRONMENT)).getValues();
        bundleInfo.setExecutionEnvironments(environments);

        parseRequirement(bundleInfo, mainAttributes, REQUIRE_BUNDLE, BundleInfo.BUNDLE_TYPE, ATTR_BUNDLE_VERSION);
        parseRequirement(bundleInfo, mainAttributes, IMPORT_PACKAGE, BundleInfo.PACKAGE_TYPE, ATTR_VERSION);
        parseRequirement(bundleInfo, mainAttributes, IMPORT_SERVICE, BundleInfo.SERVICE_TYPE, ATTR_VERSION);
        
        parseClasspaths(bundleInfo, mainAttributes, BUNDLE_CLASSPATH);

        ManifestHeaderValue exportElements = new ManifestHeaderValue(mainAttributes.getValue(EXPORT_PACKAGE));
        for (ManifestHeaderElement exportElement : exportElements.getElements()) {
            String vExport = exportElement.getAttributes().get(ATTR_VERSION);
            Version v = null;
            try {
                v = versionOf(vExport);
            } catch (NumberFormatException e) {
                throw new ParseException("The " + EXPORT_PACKAGE + " has an incorrect version: " + vExport + " ("
                        + e.getMessage() + ")", 0);
            }

            for (String name : exportElement.getValues()) {
                ExportPackage export = new ExportPackage(name, v);
                String uses = exportElement.getDirectives().get(ATTR_USE);
                if (uses != null) {
                    String[] split = uses.trim().split(",");
                    for (String u : split) {
                        export.addUse(u.trim());
                    }
                }
                bundleInfo.addCapability(export);
            }
        }

        parseCapability(bundleInfo, mainAttributes, EXPORT_SERVICE, BundleInfo.SERVICE_TYPE);

        return bundleInfo;
    }

    private static void parseRequirement(BundleInfo bundleInfo, Attributes mainAttributes, String headerName,
            String type, String versionAttr) throws ParseException {
        ManifestHeaderValue elements = new ManifestHeaderValue(mainAttributes.getValue(headerName));
        for (ManifestHeaderElement element : elements.getElements()) {
            String resolution = element.getDirectives().get(ATTR_RESOLUTION);
            String attVersion = element.getAttributes().get(versionAttr);
            VersionRange version = null;
            try {
                version = versionRangeOf(attVersion);
            } catch (ParseException e) {
                throw new ParseException("The " + headerName + " has an incorrect version: " + attVersion + " ("
                        + e.getMessage() + ")", 0);
            }

            for (String name : element.getValues()) {
                bundleInfo.addRequirement(new BundleRequirement(type, name, version, resolution));
            }
        }
    }

    private static void parseCapability(BundleInfo bundleInfo, Attributes mainAttributes, String headerName, String type)
            throws ParseException {
        ManifestHeaderValue elements = new ManifestHeaderValue(mainAttributes.getValue(headerName));
        for (ManifestHeaderElement element : elements.getElements()) {
            String attVersion = element.getAttributes().get(ATTR_VERSION);
            Version version = null;
            try {
                version = versionOf(attVersion);
            } catch (NumberFormatException e) {
                throw new ParseException("The " + headerName + " has an incorrect version: " + attVersion + " ("
                        + e.getMessage() + ")", 0);
            }

            for (String name : element.getValues()) {
                BundleCapability export = new BundleCapability(type, name, version);
                bundleInfo.addCapability(export);
            }
        }

    }
    
    private static void parseClasspaths(BundleInfo bundleInfo, Attributes mainAttributes, String headerName)
        throws ParseException {
    ManifestHeaderValue elements = new ManifestHeaderValue(mainAttributes.getValue(headerName));
    for (ManifestHeaderElement element : elements.getElements()) {

        for (String name : element.getValues()) {
            BundleClasspath classpath = new BundleClasspath(name);
            bundleInfo.addClasspath(classpath);
        }
    }

}

    private static VersionRange versionRangeOf(String v) throws ParseException {
        if (v == null) {
            return null;
        }
        return new VersionRange(v);
    }

    private static Version versionOf(String v) throws NumberFormatException {
        if (v == null) {
            return null;
        }
        return new Version(v);
    }

}
