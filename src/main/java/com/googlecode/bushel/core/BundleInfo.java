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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.googlecode.bushel.util.Version;

/**
 * Bundle info extracted from the bundle manifest.
 * 
 */
public class BundleInfo {

    public static final Version DEFAULT_VERSION = new Version(1, 0, 0, null);

    public static final String PACKAGE_TYPE = "package";

    public static final String BUNDLE_TYPE = "bundle";

    public static final String SERVICE_TYPE = "service";

    private String symbolicName;

    private String presentationName;

    private String id;

    private Version version;

    private Set<BundleRequirement> requirements = new LinkedHashSet<BundleRequirement>();

    private Set<BundleCapability> capabilities = new LinkedHashSet<BundleCapability>();
    
    private Set<BundleClasspath> classpaths = new LinkedHashSet<BundleClasspath>();

    private List<String> executionEnvironments = Collections.emptyList();

    private String description;

    private String documentation;

    private String license;

    private Integer size;

    private String uri;

    public BundleInfo(String name, Version version) {
        this.symbolicName = name;
        this.version = version;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BundleInfo [executionEnvironments=");
        builder.append(executionEnvironments);
        builder.append(", capabilities=");
        builder.append(capabilities);
        builder.append(", classpaths=");
        builder.append(classpaths);
        builder.append(", requirements=");
        builder.append(requirements);
        builder.append(", symbolicName=");
        builder.append(symbolicName);
        builder.append(", version=");
        builder.append(version);
        builder.append("]");
        return builder.toString();
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    public Version getVersion() {
        return version == null ? DEFAULT_VERSION : version;
    }

    public Version getRawVersion() {
        return version;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
    
    public String getUri() {
        return uri;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setPresentationName(String presentationName) {
        this.presentationName = presentationName;
    }

    public String getPresentationName() {
        return presentationName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getLicense() {
        return license;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getSize() {
        return size;
    }

    public void addRequirement(BundleRequirement requirement) {
        requirements.add(requirement);
    }

    public Set<BundleRequirement> getRequirements() {
        return requirements;
    }

    public void addCapability(BundleCapability capability) {
        capabilities.add(capability);
    }

    public Set<BundleCapability> getCapabilities() {
        return capabilities;
    }
    
    public void addClasspath(BundleClasspath classpath) {
      classpaths.add(classpath);
    }

    public Set<BundleClasspath> getClasspaths() {
        return classpaths;
    }

    public List<String> getExecutionEnvironments() {
        return executionEnvironments;
    }

    public void setExecutionEnvironments(List<String> executionEnvironment) {
        this.executionEnvironments = executionEnvironment;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((capabilities == null) ? 0 : capabilities.hashCode());
        result = prime * result + ((requirements == null) ? 0 : requirements.hashCode());
        result = prime * result + ((classpaths == null) ? 0 : classpaths.hashCode());
        result = prime * result + ((symbolicName == null) ? 0 : symbolicName.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        result = prime * result + ((executionEnvironments == null) ? 0 : executionEnvironments.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BundleInfo)) {
            return false;
        }
        BundleInfo other = (BundleInfo) obj;
        if (capabilities == null) {
            if (other.capabilities != null) {
                return false;
            }
        } else if (!capabilities.equals(other.capabilities)) {
            return false;
        }
        if (classpaths == null) {
          if (other.classpaths != null) {
              return false;
          }
        } else if (!classpaths.equals(other.classpaths)) {
            return false;
        }
        if (requirements == null) {
            if (other.requirements != null) {
                return false;
            }
        } else if (!requirements.equals(other.requirements)) {
            return false;
        }
        if (symbolicName == null) {
            if (other.symbolicName != null) {
                return false;
            }
        } else if (!symbolicName.equals(other.symbolicName)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        if (executionEnvironments == null) {
            if (other.executionEnvironments != null) {
                return false;
            }
        } else if (!executionEnvironments.equals(other.executionEnvironments)) {
            return false;
        }
        return true;
    }

    @Deprecated
    public Set<BundleRequirement> getRequires() {
        Set<BundleRequirement> set = new LinkedHashSet<BundleRequirement>();
        for (BundleRequirement requirement : requirements) {
            if (requirement.getType().equals(BUNDLE_TYPE)) {
                set.add(requirement);
            }
        }
        return set;
    }

    @Deprecated
    public Set<BundleRequirement> getImports() {
        Set<BundleRequirement> set = new LinkedHashSet<BundleRequirement>();
        for (BundleRequirement requirement : requirements) {
            if (requirement.getType().equals(PACKAGE_TYPE)) {
                set.add(requirement);
            }
        }
        return set;
    }

    @Deprecated
    public Set<ExportPackage> getExports() {
        Set<ExportPackage> set = new LinkedHashSet<ExportPackage>();
        for (BundleCapability capability : capabilities) {
            if (capability.getType().equals(PACKAGE_TYPE)) {
                set.add((ExportPackage) capability);
            }
        }
        return set;
    }

    @Deprecated
    public Set<BundleCapability> getServices() {
        Set<BundleCapability> set = new LinkedHashSet<BundleCapability>();
        for (BundleCapability capability : capabilities) {
            if (capability.getType().equals(SERVICE_TYPE)) {
                set.add(capability);
            }
        }
        return set;
    }

}