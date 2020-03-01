package dev.jeka.plugins.protobuf;

import dev.jeka.core.api.depmanagement.JkDependencySet;
import dev.jeka.core.api.file.JkPathTree;
import dev.jeka.core.api.java.project.JkProjectSourceLayout;
import dev.jeka.core.api.system.JkLog;
import dev.jeka.core.api.utils.JkUtilsString;
import dev.jeka.core.tool.JkCommandSet;
import dev.jeka.core.tool.JkConstants;
import dev.jeka.core.tool.JkDoc;
import dev.jeka.core.tool.JkPlugin;
import dev.jeka.core.tool.builtins.java.JkPluginJava;

import java.nio.file.Path;
import java.util.Arrays;

@JkDoc("Compiles protocol buffer files to javaPlugin source.")
public class JkPluginProtobuf extends JkPlugin {

    private static final String DEFAULT_OUT = JkConstants.OUTPUT_PATH + "/" + JkProjectSourceLayout.GENERATED_SOURCE_PATH;

    @JkDoc("Relative path of the protocol buffer files.")
    public String protoFilePath = "src/main/protobuf";

    @JkDoc("Location where .java files are generated.")
    public String outPath = DEFAULT_OUT;

    @JkDoc("Extra arguments to add to 'protoc' command.")
    public String extraArgs = "";

    @JkDoc("The version of Protocol Buffer to add to the project compile classpath (only relevant id using JkPluginJava plugin.")
    public String javaProtocolBufferVersion = "3.8.0";


    protected JkPluginProtobuf(JkCommandSet commands) {
        super(commands);
    }

    @JkDoc("Add protocol buffer source generation to the Java Project Maker. " +
            "The source generation will be automatically run prior compilation phase.")
    @Override
    protected void activate() {
        if (javaPlugin() != null) {
            javaPlugin().getProject().getMaker().getTasksForCompilation().getPreCompile().chain(this::compile);
            javaPlugin().getProject().addDependencies(JkDependencySet.of(protobufModuleVersion()));
        }
    }

    @JkDoc("Compiles protocol buffer files to javaPlugin.")
    public void compile() {
        JkLog.startTask("Compiling protocol buffer files from " + protoFilePath);
        JkPathTree protoFiles = getCommandSet().getBaseTree().goTo(protoFilePath);
        String[] extraArguments = JkUtilsString.translateCommandline(extraArgs);
        final Path out;
        if (javaPlugin() == null || !DEFAULT_OUT.equals(outPath)) {
            out = getCommandSet().getBaseDir().resolve(outPath);
        } else {
            out = javaPlugin().getProject().getMaker().getOutLayout().getGeneratedSourceDir();
        }
        JkProtobufWrapper.compile(protoFiles, Arrays.asList(extraArguments), out);
        JkLog.endTask();
    }

    private JkPluginJava javaPlugin() {
        if (getCommandSet().getPlugins().hasLoaded(JkPluginJava.class)) {
            return getCommandSet().getPlugins().get(JkPluginJava.class);
        }
        return null;
    }

    @Override
    protected String getLowestJekaCompatibleVersion() {
        return "0.8.18.RELEASE";
    }

    private String protobufModuleVersion() {
        return "com.google.protobuf:protobuf-java:" + javaProtocolBufferVersion;
    }

}
