# Compatibility Notes

## 현재 선택

요청 조건 중 `Java 17`, `Tomcat 10`, `Jakarta`, `JSP`를 우선 적용했습니다. 그래서 프로젝트는 Spring MVC 6.2.x와 `jakarta.*` API를 사용합니다.

## eGovFrame 4.x와 Tomcat 10의 충돌

eGovFrame 4.3.0 공식 런타임은 Spring Framework 5.3.37 기반입니다. Spring 5.3 계열은 Java EE 7-8, 즉 `javax.*` 네임스페이스 기반입니다.

반면 Tomcat 10.1은 Servlet 6.0 기반 Jakarta 런타임이며 `jakarta.*` 네임스페이스를 사용합니다. Spring 6 계열은 Java 17과 Jakarta EE 9 이상을 기준으로 하므로 Tomcat 10과 맞습니다.

따라서 실무 선택지는 둘 중 하나입니다.

1. eGovFrame 4.3 런타임을 반드시 사용한다면 Tomcat 9.x와 `javax.*` 기준으로 맞춥니다.
2. Tomcat 10/Jakarta를 반드시 사용한다면 Spring 6/Jakarta 기준으로 구현하고, eGov 4.x 런타임 직접 의존성은 제외합니다.
3. 기관/프로젝트 표준이 허용한다면 eGovFrame 5.x 계열 검토가 Jakarta 전환 방향과 더 자연스럽습니다.

이 프로젝트는 2번 기준으로 초기화되어 있습니다.
