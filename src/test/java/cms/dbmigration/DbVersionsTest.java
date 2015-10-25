package eu.cycleon.dbmigration;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.junit.Assert.*;


public class DbVersionsTest {
    /**
     * pattern : V[version]__[description].sql
     * where version can contain only 0..9 and .
     *
     * $1 = version, $2 = description
     */
    public static final String FILE_PATTERN = "^V([0-9.]+)__(.+)\\.sql$";

    private List<File> files;
    private Pattern pattern;

    @Before
    public void setUp() throws Exception {
        pattern = Pattern.compile(FILE_PATTERN);
        files = loadUpgradeSqlFiles();
    }

    @Test
    public void testAllFilesMatchPattern() throws Exception {
        for (File file : files) {
            Matcher matcher = pattern.matcher(file.getName());
            assertTrue(format("%s does not match %s", file.getName(), FILE_PATTERN),
                    matcher.matches());
        }
    }

    @Test
    public void testVersionsAreUnique() throws Exception {
        Map<String, String> versionVsFile = new HashMap<String, String>();
        for (File file : files) {
            String version = getFileVersion(file);
            assertNotNull("bad version for " + file.getName(), version);
            assertFalse(format("duplicate version found within files %s and %s", versionVsFile.get(version), file.getName()),
                    versionVsFile.containsKey(version));
            versionVsFile.put(version, file.getName());
        }
    }

    private String getFileVersion(File file) {
        Matcher matcher = pattern.matcher(file.getName());
        if(matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    @SuppressWarnings("ConstantConditions")
    private List<File> loadUpgradeSqlFiles() throws URISyntaxException {
        URI sqlDir = this.getClass().getClassLoader().getResource("sql").toURI();
        File[] sqlFiles = new File(sqlDir).listFiles();
        return Arrays.asList(sqlFiles);
    }
}