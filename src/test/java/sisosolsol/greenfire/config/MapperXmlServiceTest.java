package sisosolsol.greenfire.config;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * 모든 MyBatis 매퍼 XML을 DB 없이 파싱해 문법·참조 오류를 잡는다.
 * (XML 오류는 서버를 띄워야만 드러나므로 단위 테스트 단계에서 미리 확인.
 *  CI 패턴 *ServiceTest 에 포함되도록 이름을 맞춤)
 */
class MapperXmlServiceTest {

    @Test
    @DisplayName("mappers/**/*.xml 전체가 파싱되고, 모든 statement의 SQL 조각(include)이 해석된다")
    void allMapperXmlParse() throws Exception {
        Configuration configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.getTypeHandlerRegistry().register("sisosolsol.greenfire.common.handler");

        Resource[] xmls = new PathMatchingResourcePatternResolver().getResources("classpath*:mappers/**/*.xml");
        assertThat(xmls).isNotEmpty();

        Map<String, String> failures = new HashMap<>();
        for (Resource xml : xmls) {
            try (InputStream in = xml.getInputStream()) {
                new XMLMapperBuilder(in, configuration, xml.getURL().toString(), configuration.getSqlFragments())
                        .parse();
            } catch (Exception e) {
                failures.put(xml.getFilename(), e.getMessage());
            }
        }
        assertThat(failures).as("파싱 실패한 매퍼").isEmpty();

        // 다른 매퍼를 참조하는 include 등 지연 해석 대상까지 모두 끝났는지.
        // 짧은 이름은 매퍼 간 중복될 수 있어 namespace 를 포함한 전체 이름으로만 조회
        List<String> fullNames = configuration.getMappedStatementNames().stream()
                .filter(name -> name.contains("."))
                .toList();
        assertThat(fullNames).isNotEmpty();
        for (String name : fullNames) {
            assertThatCode(() -> configuration.getMappedStatement(name)).as(name).doesNotThrowAnyException();
            MappedStatement statement = configuration.getMappedStatement(name);
            assertThat(statement.getSqlSource()).as(name).isNotNull();
        }
        assertThat(configuration.getIncompleteStatements()).as("해석되지 못한 statement").isEmpty();
    }
}
