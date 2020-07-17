package liquibase.wrapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.resource.AbstractResourceAccessor;
import liquibase.resource.InputStreamList;

public class StringResourceAccessor extends AbstractResourceAccessor {

	//FIXME: this can easily produce a collision with actual "foobar.xml" files 
    public static final String ANY_XML_FILENAME = "foobar.xml";
    
	private String content;

    public StringResourceAccessor(String content) {
		this.content = content;
    }

	@Override
	public InputStreamList openStreams(String relativeTo, String path) throws IOException {
		if (!ANY_XML_FILENAME.equals(path)) {
			return null;
		}
        InputStreamList list = new InputStreamList();
        ByteArrayInputStream stream = new ByteArrayInputStream(content.getBytes(LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputEncoding()));
        list.add(URI.create(path), stream);
        return list;
    }

    @Override
    public SortedSet<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
    	SortedSet<String> returnSet = new TreeSet<>();
    	if (ANY_XML_FILENAME.startsWith(path)) {
    		returnSet.add(ANY_XML_FILENAME);
        }
    	
    	return returnSet;
    }

    @Override
    public SortedSet<String> describeLocations() {
        return new TreeSet<String>(Collections.singletonList("StringResourceAccessor.java"));
    }
}
