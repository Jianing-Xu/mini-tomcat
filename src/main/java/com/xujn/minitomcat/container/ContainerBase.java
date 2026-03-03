package com.xujn.minitomcat.container;

import com.xujn.minitomcat.bootstrap.LifecycleBase;
import com.xujn.minitomcat.pipeline.Pipeline;
import com.xujn.minitomcat.pipeline.StandardPipeline;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared container base that manages name, parent, children, and the reserved pipeline.
 *
 * <p>Key constraint: child mutation is only allowed during startup assembly.
 * Thread safety assumption: request threads read the child map after startup has frozen the tree.</p>
 */
public abstract class ContainerBase extends LifecycleBase implements Container {

    private final String name;
    private final Map<String, Container> children = new LinkedHashMap<>();
    private final Pipeline pipeline = new StandardPipeline();
    private Container parent;

    protected ContainerBase(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Container getParent() {
        return parent;
    }

    @Override
    public void setParent(Container parent) {
        this.parent = parent;
    }

    @Override
    public void addChild(Container child) {
        if (children.containsKey(child.getName())) {
            throw new IllegalArgumentException("Duplicate child container name=" + child.getName() + " under " + name);
        }
        child.setParent(this);
        children.put(child.getName(), child);
    }

    @Override
    public Container findChild(String childName) {
        return children.get(childName);
    }

    @Override
    public Pipeline getPipeline() {
        return pipeline;
    }

    protected Map<String, Container> getChildren() {
        return children;
    }

    @Override
    protected void startInternal() {
        for (Container child : children.values()) {
            child.start();
        }
    }

    @Override
    protected void stopInternal() {
        for (Container child : children.values()) {
            // Phase 1 shutdown must destroy initialized servlets during stop.
            child.destroy();
        }
    }
}
