# Approval System

결재 시스템 연계 모듈 초기 세팅입니다. 현재 구조는 `Java 17`, `Jakarta`, `Tomcat 10.1`, `JSP` 기준의 Maven `war` 프로젝트입니다.

## 환경

- Java 17
- Maven 3.9 이상
- Tomcat 10.1.x
- Servlet 6.0 / JSP 3.1
- Spring MVC 6.2.x

## 실행

```powershell
mvn clean package
```

생성된 파일을 Tomcat 10.1의 `webapps`에 배포합니다.

```text
target/approval-system.war
```

배포 후 기본 경로:

```text
http://localhost:8080/approval-system/approval
```

## 구조

```text
src/main/java/kr/co/approval
  config                 Spring MVC 설정
  approval/web           결재 화면 컨트롤러
  approval/service       결재 서비스 인터페이스/메모리 구현
  approval/dto           문서/상태/이력/요청 DTO

src/main/webapp/WEB-INF/views/approval
  index.jsp              결재 문서 등록 및 목록 화면
  popup.jsp              팝업형 결재 처리 화면
```

## 구현된 결재 흐름

1. `/approval`에서 신규 결재 문서를 생성합니다.
2. 문서 목록의 제목을 클릭하면 선택 문서의 내용을 왼쪽 영역에서 조회합니다.
3. 선택 문서가 `작성중` 상태이면 `수정` 버튼으로 편집 모드에 들어가 저장할 수 있습니다.
4. 문서 목록의 `상세/처리` 또는 선택 문서의 `팝업 처리` 버튼으로 `/approval/popup?documentId=...` 팝업을 엽니다.
5. 작성중, 반려, 회수 문서는 팝업에서 수정, 삭제, 상신할 수 있습니다.
6. 결재요청 문서는 팝업에서 회수, 승인, 반려할 수 있습니다.
7. 팝업 하단에서 처리 이력을 확인할 수 있습니다.

## 기본 결재 로직

- 현재 인증 모듈 전 단계라 로그인 사용자는 `requester01`로 시뮬레이션합니다.
- 요청자는 화면에서 읽기 전용으로 표시되며 서버에서 로그인 사용자로 고정됩니다.
- 결재자는 직접 입력하지 않고 결재라인 선택 팝업에서 1명 이상 지정합니다.
- 결재라인을 여러 명 지정하면 순차 결재로 처리합니다.
- 앞 결재자가 승인하면 다음 결재자에게 넘어가고, 마지막 결재자가 승인하면 최종 승인됩니다.
- 요청자와 결재자는 서로 달라야 합니다.
- 요청자만 문서 수정, 상신, 회수, 삭제를 할 수 있습니다.
- 현재 순번의 결재자만 승인 또는 반려를 할 수 있습니다.
- 반려에는 반려 의견이 필수입니다.
- 승인된 문서는 더 이상 수정, 삭제, 상신할 수 없습니다.
- 회수하면 `결재요청` 상태가 다시 `작성중` 상태로 변경됩니다.
- 수정은 `작성중` 상태에서만 가능합니다.

현재 저장소는 DB 연결 전 단계라 `InMemoryApprovalService`가 메모리에 결재 문서를 보관합니다. 애플리케이션을 재시작하면 데이터는 초기화됩니다.

## eGov 4.x 관련 메모

eGovFrame 4.3 런타임은 Spring 5.3.37 기반이라 `javax.*` 네임스페이스를 사용합니다. Tomcat 10.1/Jakarta 조건을 유지하려면 Spring 6 계열이 필요하므로, 현재 세팅에는 eGov 4.3 런타임 의존성을 직접 추가하지 않았습니다. 자세한 판단 기준은 `docs/compatibility.md`에 정리했습니다.
