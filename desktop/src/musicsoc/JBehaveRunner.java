import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class JBehaveRunner extends JUnitStories {

    @Override
        public Configuration configuration() {
        return new MostUsefulConfiguration()
            .useStoryReporterBuilder(new StoryReporterBuilder().withDefaultFormats());
    }

    @Override
        protected List<String> storyPaths() {
        return Arrays.asList("src/test/resources/stories/*.story");
    }

    @Test
        public void runJBehave() {
        try {
            super.run();
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
