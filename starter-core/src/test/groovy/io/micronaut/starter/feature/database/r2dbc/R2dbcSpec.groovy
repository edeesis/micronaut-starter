package io.micronaut.starter.feature.database.r2dbc

import io.micronaut.starter.ApplicationContextSpec
import io.micronaut.starter.BuildBuilder
import io.micronaut.starter.feature.database.DatabaseDriverFeature
import io.micronaut.starter.feature.database.jdbc.JdbcFeature
import io.micronaut.starter.fixture.CommandOutputFixture
import io.micronaut.starter.options.BuildTool
import spock.lang.Unroll

class R2dbcSpec extends ApplicationContextSpec implements CommandOutputFixture {

    @Unroll
    void "test gradle r2dbc feature #driver"() {
        when:
        String template = new BuildBuilder(beanContext, BuildTool.GRADLE)
                .features(["r2dbc", driver])
                .render()
        driver = getDriverName(driver)

        then:
        template.contains("implementation(\"io.micronaut.r2dbc:micronaut-r2dbc-core\")")
        template.contains(":$driver")

        where:
        driver << (beanContext.getBeansOfType(DatabaseDriverFeature)*.name) - "oracle-cloud-atp"
    }

    @Unroll
    void "test maven r2dbc feature #driver"() {
        when:
        String template = new BuildBuilder(beanContext, BuildTool.MAVEN)
                .features(["r2dbc", driver])
                .render()
        driver = getDriverName(driver)
        JdbcFeature jdbcFeature = beanContext.getBean(JdbcFeature)

        then:
        jdbcFeature.name == 'jdbc-hikari'
        template.contains("""
    <dependency>
      <groupId>io.micronaut.r2dbc</groupId>
      <artifactId>micronaut-r2dbc-core</artifactId>
      <scope>compile</scope>
    </dependency>
""")
        !template.contains("""
    <dependency>
      <groupId>io.micronaut.sql</groupId>
      <artifactId>micronaut-jdbc-hikari</artifactId>
      <scope>compile</scope>
    </dependency>
""")
        template.contains("<artifactId>$driver</artifactId>")

        where:
        driver << (beanContext.getBeansOfType(DatabaseDriverFeature)*.name) - "oracle-cloud-atp"
    }

    def getDriverName(name) {
        if (name == 'sqlserver') {
            return "r2dbc-mssql"
        }
        if (name == 'postgres') {
            return "r2dbc-postgresql"
        }
        if (name == 'oracle') {
            return "oracle-r2dbc"
        }
        return "r2dbc-$name"
    }
}
