# /25-01-17

## 서블릿 예외 처리 - 프로젝트 생성

# /25-01-18

## 서블릿 예외 처리 - 시작

### 서블릿은 다음 2가지 방식으로 예외 처리를 지원한다.
- Exception(예외)
- response.sendError(HTTP 상태 코드, 오류 메시지)


### Exception(예외)
#### 자바 직접 실행
자바의 메인 메서드를 직접 실행하는 경우 main이라는 이름의 쓰레드가 실행된다.
실행 도중에 예외를 잡지 못하고 지금 실행한 main() 메서드를 넘어서 예외가 던져지면, 예외 정보를 남기고 해당 쓰레드는 종료된다.

### 웹 애플리케이션
웹 애플리케이션은 사용자 요청별로 별도의 쓰레드가 할당되고, 서블릿 컨테이너 안에서 실행된다.
애플리케이션에서 예외가 발생했는데, 어디선가 try ~ catch로 예외를 잡아서 처리하면 아무런 문제가 없다.
그런데 만약에 애플리케이션에서 예외를 잡지 못하고, 서블릿 밖으로 까지 예외가 전달되면 어떻게 동작할까?

WAS(여기까지 전파) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러(예외발생)

결국 톰캣 같은 WAS까지 예외가 전달된다. 

웹 브라우저에서 개발자 모드로 확인해보면 HTTP 상태 코드가 500으로 보인다.
Exception의 경우 서버 내부에서 처리할 수 없는 오류가 발생한 것으로 생각해서 HTTP 상태 코드 500을 반환한다.

### response.sendError(HTTP 상태 코드, 오류 메시지)
오류가 발생했을 때 HttpServletResponse가 제공하는 sendError라는 메서드를 사용해도 된다.
이것을 호출한다고 당장 예외가 발생하는 것은 아니지만, 서블릿 컨테이너에게 오류가 발생했다는 점을 전달할 수 있다.
이 메서드를 사용하면 HTTP 상태 코드와 오류 메시지도 추가할 수 있다.

- response.sendError(HTTP상태코드)
- response.sendError(HTTP상태코드, 오류메시지)

#### sendError호출
WAS(sendError 호출 기록 확인) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러(response.sendError())

response.sendError()를 호출하면 response 내부에는 오류가 발생했다는 상태를 저장해둔다.
그리고 서블릿 컨테이너는 고객에게 응답 전에 response에 sendError()가 호출되었는지 확인한다.
그리고 호출되었다면 설정한 오류 코드에 맞추어 기본 오류 페이지를 보여준다.

# /25-01-19

## 서블릿 예외 처리 - 오류 화면 제공
서블릿 컨테이너가 제공하는 기본 예외 처리 화면은 고객 친화적이지 않다. 서블릿이 제공하는 오류 화면 기능을 사용해보자.

과거에는 web.xml이라는 파일에 오류화면을 등록했었다.
지금은 스프링 부트를 통해서 서블릿 컨테이너를 실행하기 때문에, 스프링 부트가 제공하는 기능을 사용해서 서블릿 오류 페이지를 등록하면 된다.

# /25-01-20

## 서블릿 예외 처리 - 오류 페이지 작동 원리

### 예외 발생 흐름
WAS(여기까지 전파) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러(예외발생)

### sendError 흐름
WAS(sendError 호출 기록 확인) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러(response.sendError())

WAS는 해당 예외를 처리하는 오류 페이지 정보를 확인한다.

### 오류 페이지 요청 흐름
WAS '/error-page/500' 다시 요청 -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러(/error-page/500) -> View

### 예외 발생과 오류 페이지 요청 흐름
중요한 점은 웹 브라우저(클라이언트)는 서버 내부에서 이런 일이 일어나는지 전혀 모른다는 점이다.
오직 서버 내부에서 오류 페이지를 찾기 위해 추가적인 호출을 한다.

1. 예외가 발생해서 WAS까지 전파된다.
2. WAS는 오류 페이지 경로를 찾아서 내부에서 오류 페이지를 호출한다.
3. 이때 오류 페이지 경로로 필터, 서블릿, 인터셉터, 컨트롤러가 모두 다시 호출된다.

### request.attribute에 서버가 담아준 정보
- javax.servlet.error.exception : 예외
- javax.servlet.error.exception_type : 예외 타입
- javax.servlet.error.message : 메시지
- javax.servlet.error.request_uri : 클라이언트 URI
- javax.servlet.error.servlet_name : 오류가 발생한 서블릿 이름
- javax.servlet.error.status_code : HTTP 상태 코드

# /25-01-21

## 서블릿 예외 처리 - 필터

### 예외 발생과 오류페이지 요청 흐름
오류가 발생하면 오류 페이지를 출력하기 위해 WAS 내부에서 다시 한번 호출이 발생한다. 이때 필터, 서블릿, 인터셉터도 모두 다시 호출된다.
그런데 로그인 인증 체크 같은 경우를 생각해보면, 이미 한번 필터나, 인터셉터에서 로그인 체크를 완료했다.
따라서 서버 내부에서 오류 페이지를 호출한다고 해서 해당 필터나 인터셉트가 한번 더 호출되는 것은 매우 비효율적이다.
결국 클라이언트로 부터 발생한 정상 요청인지, 아니면 오류 페이지를 출력하기 위한 내부 요청인지 구분할 수 있어야 한다.
서블릿은 이런 문제를 해결하기 위해 DispatcherType 이라는 추가 정보를 제공한다.

### DispatcherType
필터는 이런 경우를 위해서 dispatcherTypes라는 옵션을 제공한다.

- REQUEST : 클라이언트 요청
- ERROR : 오류 요청
- FORWARD : MVC에서 배웠던 서블릿에서 다른 서블릿이나 JSP를 호출할 때
- INCLUDE : 서블릿에서 다른 서블릿이나 JSP의 결과를 포함할 때
- ASYNC : 서블릿 비동기 호출

# /25-01-22

## 서블릿 예외 처리 - 인터셉터

### 전체 흐름 정리

#### 정상 요청
WAS(/hello, dispatchType=REQUEST) -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러 -> View

#### 오류 요청
- 필터는 'DispatchType'으로 중복 호출 제거 (dispatchType=REQUEST)
- 인터셉터는 경로 정보로 중복 호출 제거 (excludePathPatterns("/error-page/**")

1. WAS(/error-ex, dispatchType=REQUEST) -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러
2. WAS(여기까지 전파) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러(예외발생)
3. WAS 오류 페이지 확인
4. WAS(/error-page/500, dispatchType=ERROR) -> 필터(x) -> 서블릿 -> 인터셉터(x) -> 컨트롤러(/error-page/500) -> View

# /25-01-24

## 스프링 부트 - 오류 페이지1

지금까지 예외 처리 페이지를 만들기 위해서 다음과 같은 복잡한 과정을 거쳤다
- WebServerCustomizer를 만들고
- 예외 종류에 따라서 ErrorPage를 추가하고
- 예외 처리용 컨트롤러 ErrorPageController를 만듬

### 스프링부트는 이런 과정을 모두 기본으로 제공한다
- ErrorPage를 자동으로 등록한다. 이 때 /error 라는 경로로 기본 오류 페이지를 설정한다
  - new ErrorPage("/error"), 상태 코드와 예외를 설정하지 않으면 기본 오류 페이지로 사용된다.
  - 서블릿 밖으로 예외가 발생하거나, response.sendError(...)가 호출되면 모든 오류는 /error를 호출하게 된다.
- BasicErrorController라는 스프링 컨트롤러를 자동으로 등록한다
  - ErrorPage에서 등록한 /error를 매핑해서 처리하는 컨트롤러다
 
### 참고
ErrorMvcAutoConfiguration이라는 클래스가 오류 페이지를 자동으로 등록하는 역할을 한다

### 주의
스프링 부트가 제공하는 기본 오류 메커니즘을 사용하도록 WebServerCustomizer에 있는 @Component를 주석 처리하자

이제 요류가 발생했을 때 오류 페이지로 /error를 기본 요청한다. 스프링 부트가 자동 등록한 BasicErrorController는 이 경로를 기본으로 받는다.

### 개발자는 오류 페이지만 등록
BasicErrorController는 기본적인 로직이 모두 개발되어 있다
개발자는 오류 페이지 화면만 BasicErrorController가 제공하는 룰과 우선순위에 따라서 등록하면 된다.
정적 HTML이면 정적 리소스, 뷰 템플릿을 사용해서 동적으로 오류 화면을 만들고 싶으면 뷰 템플릿 경로에 오류 페이지 파일을 만들어서 넣어두기만 하면 된다.

### 뷰 선택 우선순위
BasicErrorController의 처리 순위
1. 뷰 템플릿
   - resource/templates/error/500.html
2. 정적 리소스(static, public)
   - resource/static/error/400.html
   - resource/static/error/404.html
3. 적응 대상이 없을 때 뷰 이름(error)
   - resource/templates/error.html

해당 경로 위치에 HTTP 상태 코드 이름의 뷰 파일을 넣어두면 된다.

# /25-01-25

## 스트링 부트 - 오류 페이지2

오류 관련 내부 정보들을 고객에게 노출하는 것은 좋지 않다. 고객이 해당 정보를 읽어도 혼란만 더해지고, 보안상 문제가 될 수도 있다.
그래서 BasicErrorController 오류 컨트롤러에서 다음 오류 정보를 model에 포함할지 여부를 선택할 수 있다.
application.properties
server.error.include-exception=false : exception 포함 여부( true , false ) 
server.error.include-message=never : message 포함 여부 
server.error.include-stacktrace=never : trace 포함 여부 
server.error.include-binding-errors=never : errors 포함 여부

- never : 사용하지 않음
- always : 항상 사용
- on_param : 파라미터가 있을 때 사용

on_param은 파라미터가 있으면 해당 정보를 노출한다. 
디버그 시 문제를 확인하기 위해 사용할 수 있다. 
그런데 이 부분도 개발 서버에서 사용할 수 있지만, 운영 서버에서는 권장하지 않는다.

### 주의
실무에서는 이것들을 노출하면 안된다! 사용자에게는 이쁜 오류 화면과 고객이 이해할 수 있는 간단한 오류 메시지를 보여주고
오류는 서버에 로그로 남겨서 로그로 확인해야 한다.

### 스프링 부트 오류 관련 옵션
- server.error.whitelabel.enabled=true : 오류 처리 화면을 못찾을 시, 스프링 whitelabel 오류 페이지 적용
- server.error.path=/error : 오류 페이지 경로, 스프링이 자동 등록하는 서블릿 글로벌 오류 페이지 경로와 BasicErrorController 오류 컨트롤러 경로에 함께 사용

### 확장 포인트
에러 공통 처리 컨트롤러의 기능을 변경하고 싶으면 ErrorController 인터페이스를 상속 받아서 구현하거나 BasicErrorController 상속 받아서 기능을 추가하면 된다.

