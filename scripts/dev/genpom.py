"""Herramienta de desarrollo: genera el pom.xml de un microservicio LicitaWatch.

Uso: python scripts/dev/genpom.py <carpeta_modulo> <artifactId> "<nombre>" "<descripcion>" dep1 dep2 ...
Dependencias abreviadas: common web jpa validation actuator flyway postgres springdoc lombok test
                         crypto jjwt mail thymeleaf transbank
"""
import os
import sys

DEPS = {
    "common": [("cl.licitawatch", "licitawatch-common", None)],
    "web": [("org.springframework.boot", "spring-boot-starter-web", None)],
    "jpa": [("org.springframework.boot", "spring-boot-starter-data-jpa", None)],
    "validation": [("org.springframework.boot", "spring-boot-starter-validation", None)],
    "actuator": [("org.springframework.boot", "spring-boot-starter-actuator", None)],
    "flyway": [("org.flywaydb", "flyway-core", None), ("org.flywaydb", "flyway-database-postgresql", None)],
    "postgres": [("org.postgresql", "postgresql", "runtime")],
    "springdoc": [("org.springdoc", "springdoc-openapi-starter-webmvc-ui", None)],
    "crypto": [("org.springframework.security", "spring-security-crypto", None)],
    "jjwt": [("io.jsonwebtoken", "jjwt-api", None), ("io.jsonwebtoken", "jjwt-impl", "runtime"),
             ("io.jsonwebtoken", "jjwt-jackson", "runtime")],
    "mail": [("org.springframework.boot", "spring-boot-starter-mail", None)],
    "thymeleaf": [("org.springframework.boot", "spring-boot-starter-thymeleaf", None)],
    "transbank": [("com.github.transbankdevelopers", "transbank-sdk-java", None, "6.2.1")],
    "lombok": [("org.projectlombok", "lombok", "optional")],
    "test": [("org.springframework.boot", "spring-boot-starter-test", "test")],
}


def dep_xml(d):
    g, a, scope = d[0], d[1], d[2]
    version = d[3] if len(d) > 3 else None
    out = ["        <dependency>", f"            <groupId>{g}</groupId>", f"            <artifactId>{a}</artifactId>"]
    if version:
        out.append(f"            <version>{version}</version>")
    if scope == "optional":
        out.append("            <optional>true</optional>")
    elif scope:
        out.append(f"            <scope>{scope}</scope>")
    out.append("        </dependency>")
    return "\n".join(out)


def main():
    carpeta, artifact, nombre, descripcion, *deps = sys.argv[1:]
    profundidad = len(os.path.normpath(carpeta).split(os.sep))
    relative = "/".join([".."] * profundidad) + "/pom.xml"
    cuerpo = "\n".join(dep_xml(d) for k in deps for d in DEPS[k])
    xml = f"""<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>cl.licitawatch</groupId>
        <artifactId>licitawatch-parent</artifactId>
        <version>2.0.0</version>
        <relativePath>{relative}</relativePath>
    </parent>
    <artifactId>{artifact}</artifactId>
    <name>{nombre}</name>
    <description>{descripcion}</description>

    <dependencies>
{cuerpo}
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
"""
    os.makedirs(carpeta, exist_ok=True)
    with open(os.path.join(carpeta, "pom.xml"), "w", encoding="utf-8", newline="\n") as f:
        f.write(xml)
    print("pom generado:", os.path.join(carpeta, "pom.xml"))


if __name__ == "__main__":
    main()
