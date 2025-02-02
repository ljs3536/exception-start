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

# /25-01-26

## API 예외 처리 - 시작
HTML 페이지의 경우 지금까지 설명했던 것 처럼 4xx, 5xx와 같은 오류 페이지만 있으면 대부분의 문제를 해결할 수 있다.
그런데 API의 경우에는 생각할 내용이 더 많다. 오류 페이지는 단순히 고객에게 오류 화면을 보여주고 끝이지만, API는 각 오류 상황에 맞는 오류 응답 스펙을 정하고,
JSON으로 데이터를 내려주어야 한다.


### 예외 발생 호출
API를 요청했는데, 정상의 경우 API로 JSON 형식으로 데이터가 정상 반환된다. 
그런데 오류가 발생하면 우리가 미리 만들어둔 오류 페이지 HTML이 반환된다. 
이것은 기대하는 바가 아니다. 클라이언트는 정상 요청이든, 오류 요청이든 JSON이 반화노디기를 기대한다.
웹 브라우저가 아닌 이상 HTML을 직접 받아서 할 수 있는 것은 별로 없다.

문제를 해결하려면 오류 페이지 컨트롤러도 JSON 응답을 할 수 있도록 수정해야 한다.

# /25-01-27

## API 예외처리 - 스프링 부트 기본 오류 처리 

스프링 부트가 제공하는 BasicErrorController 코드를 보자

### BasicErrorController 코드
- errorHtml() : produces = MediaType.TEXT_HTML_VALUE : 클라이언트 요청의 Accept 헤더 같이 text/html인 경우에는 errorHtml()을 호출해서 view를 제공한다.
- error() : 그 외 경우에 호출되고, ResponseEntity로 HTTP Body에 JSON 데이터를 반환한다.

### 스프링 부트의 예외처리
스프링 부트의 기본 설정은 오류 발생 시 /error를 오류 페이지로 요청한다. 
BasicErrorController는 이 경로를 기본으로 받는다. (server.error.path로 수정 가능. 기본경로 /error)

### HTML페이지 vs API오류
BasicErrorController를 확장하면 JSON 메시지도 변경할 수 있다.
스프링 부트가 제공하는 BasicErrorController는 HTML 페이지를 제공하는 경우에는 매우 편리하다.
그런데 API 오류 처리는 다른 차원의 이야기이다. 
API 마다, 각각의 컨트롤러나 예외마다 서로 다른 응답 결과를 출력해야 할 수도 있다.
예를 들어서 회원과 관련된 API에서 예외가 발생할 때 응답과, 상품과 관련된 API에서 발생하는 예외에 따라 그 결과가 달라질 수 있다.
결과적으로 매우 세밀하고 복잡하다. 
따라서 이 방법은 HTML 화면을 처리할 때 사용하고, API 오류 처리는 뒤에서 설명할 @ExceptionHandler를 사용하자

# /25-01-29

## API 예외 처리 - HandlerExceptionResolver 시작

### 목표
예외가 발생해서 서블릿을 넘어 WAS까지 예외가 전달되면 HTTP 상태코드가 500으로 처리된다. 
발생하는 예외에 따라서 400,404 등등 다른 상태코드도 처리하고 싶다.
오류 메시지, 형식등을 API마다 다르게 처리하고 싶다.

### 상태코드 변환
예를 들어서 IllegalArgumentException을 처리하지 못해서 컨트롤러 밖으로 넘어가는 일이 발생하면 
HTTP 상태코드를 400으로 처리하고 싶다. 어떻게 해야할까?

### HandlerExceptionResolver
스프링 MVC는 컨트롤러(핸들러) 밖으로 예외가 던져진 경우 예외를 해결하고, 동작을 새로 정의할 수 있는 방법을 제공한다.
컨트롤러 밖으로 던져진 예외를 해결하고, 동작 방식을 변경하고 싶으면 HandlerExceptionResolver를 사용하면 된다.
줄여서 ExceptionResolver라 한다.

### 참고 : ExceptionResolber로 예외를 해결해도 PostHandler()은 호출되지 않는다.

### 반환값에 따른 동작방식
HandlerExceptionResolver의 반환 값에 따른 DispatcherServlet의 동작 방식은 다음과 같다
- 빈 ModelAndView : new ModelAndView()처럼 빈 ModelAndView를 반환하면 뷰를 렌더링 하지 않고, 정상 흐름으로 서블릿이 리턴된다.
- ModelAndView 지정 : ModelAndView에 View, Model 등의 정보를 지정해서 반환하면 뷰를 렌더링 한다.
- null : null을 반환하면, 다음 ExceptionResolver를 찾아서 실행한다. 만약 처리할 수 있는 ExceptionResolver가 없으면 예외처리가 안되고, 기존에 발생한 예외를 서블릿 밖으로 던진다.


### ExceptionResolver 활용
- 예외 상태 코드 변환
  - 예외를 response.sendError(xxx) 호출로 변경해서 서블릿에서 상태 코드에 따른 오류를 처리하도록 위임
  - 이후 WAS는 서블릿 오류 페이지를 찾아서 내부 호출, 예를 들어서 스프링 부트가 기본으로 설정한 /error가 호출됨
- 뷰 템플릿 처리
  - ModelAndView에 값을 채워서 예외에 따른 새로운 오류 화면 뷰 렌더링 해서 고객에서 제공
- API 응답 처리
  - response.getWriter().println("hello"); 처럼 HTTP응답 바디에 직접 데이터를 넣어주는 것도 가능하다.
    여기에 JSON으로 응답하면 API응답 처리를 할 수 있다.

# /25-01-30

## API 예외 처리 - HandlerExceptionResolver 활용

### 정리
ExceptionResolver를 사용하면 컨트롤러에서 예외가 발생해도 ExceptionResolver에서 예외를 처리해버린다.
따라서 예외가 발생해도 서블릿 컨테이너까지 예외가 전달되지 않고, 스프링 MVC에서 예외 처리는 끝이 난다.
결과적으로 WAS 입장에서는 정상 처리가 된 것이다. 이렇게 예외를 이곳에서 모두 처리할 수 있다는 것이 핵심이다.

서블릿 컨테이너까지 예외가 올라가면 복잡하고 지저분하게 추가 프로세스가 실행된다.
반면에 ExceptionResolver를 사용하면 예외 처리가 상당히 깔끔해진다.

# /25-01-31

## API 예외 처리 - 스프링이 제공하는 ExceptionResolver1
스프링 부트가 기본으로 제공하는 ExceptionResolver는 다음과 같다.
HandlerExceptionResolverComposite에 다음 순서로 등록
1. ExceptionHandlerExceptionResolver
2. ResponseStatusExceptionResolver
3. DefaultHandlerExceptionResolver > 우선 순위가 가장 낮다.

### ExceptionHandlerExceptionResolver
@ExceptionHandler을 처리한다. API 예외 처리는 대부분 이 기능으로 해결한다.

### ResponseStatusExceptionResolver
HTTP 상태 코드를 지정해준다.

### DefaultHandlerExceptionResolver
스프링 내부 기본 예외를 처리한다.

### ResponseStatusExceptionResolver
- @ResponseStatus가 달려있는 예외
- ResponseStatusException 예외

# /25-02-01

## API 예외 처리 - 스프링이 제공하는 ExceptionResolver2

### DefaultHandlerExceptionResolver
DefaultHandlerExceptionResolver는  스프링 내부에서 발생하는 스프링 예외를 해결한다.
대표적으로 파라미터 바인딩 시점에 타입이 맞지 않으면 내부에서 TypeMismatchException이 발생하는데, 
이 경우 예외가 발생했기 때문에 그냥 두면 서블릿 컨테이너까지 오류가 올라가고, 결과적으로 500 오류가 발생한다.
그런데 파라미터 바인딩은 대부분 클라이언트가 HTTP 요청 정보를 잘못 호출해서 발생하는 문제이다.
HTTP에서는 이런 경우 HTTP 상태 코드 400을 사용하도록 되어 있다.
DefaultHandlerExceptionResolver는 이것을 500 오류가 아니라 HTTP 상태 코드 400 오류로 변경한다.
스프링 내부 오류를 어떻게 처리할지 수 많은 내용이 정의되어 있다.

### 정리
1. ExceptionHandlerExceptionResolver
2. ResponseStatusExceptionResolver -> HTTP 응답 코드 변경
3. DefaultHandlerExceptionResolver -> 스프링 내부 예외 처리

지금까지 HTTP 상태 코드를 변경하고, 스프링 내부 예외의 상태코드를 변경하는 기능도 알아보았다.
그런데 HandlerExceptionResolver를 직접 사용하기는 복잡하다. 
API오류 응답의 경우 response에 직접 데이터를 넣어야 해서 매우 불편하고 번거롭다.
ModelAndView를 반환해야 하는 것도 API에는 잘 맞지 않는다.
스프링은 이 문제를 해결하기 위해 @ExceptionHandler라는 매우 혁신적인 예외 처리 기능을 제공한다.
다음에 알아볼 ExceptionHandlerExceptionResolver이다.

# /25-02-02

## API 예외 처리 - @ExceptionHandler

### HTML 화면 오류 vs API 오류
웹 브라우저에 HTML 화면을 제공할 때는 오류가 발생하면 BasicErrorController를 사용하는게 편하다.
이때는 단순히 5xx, 4xx 관련된 오류 화면을 보여주면 된다.
BasicErrorController는 이런 메커니즘을 모두 구현해두었다.

### API 예외 처리의 어려운 점
- HandlerExceptionResolver를 떠올려 보면 ModelAndView를 반환해야 했다. 이것은 API 응답에는 필요하지 않다.
- API응답을 위해서 HttpServletResponse에 직접 응답 데이터를 넣어주었다. 이것은 매우 불편하다.
  스프링 컨트롤러에 비유하면 마치 과거 서블릿을 사용하던 시절로 돌아간 것 같다.
- 특정 컨트롤러에서만 발생하는 예외를 별도로 처리하기 어렵다. 예를 들어서 회원을 처리하는 컨트롤러에서 발생하는 RuntimeException 예외와
  상품을 관리하는 컨트롤러에서 발생하는 동일한 RuntimeException 예외를 서로 다른 방식으로 처리하고 싶다면 어떻게 해야 할까?

### @ExceptionHandler
스프링은 API 예외 처리 문제를 해결하기 위해 @ExceptionHandler라는 애노테이션을 사용하는 매우 편리한 예외 처리 기능을 제공하는데, 
이것이 바로 ExceptionHandlerExceptionResolver이다. 스프링은 이 리졸버를 기본으로 제공하고 우선순위도 가장 높다.

### @ExceptionHandler 예외 처리 방법
@ExceptionHandler 에노테이션을 선언하고, 해당 컨트롤러에서 처리하고 싶은 예외를 지정해주면 된다.
해당 컨트롤러에서 예외가 발생하면 이 메서드가 호출된다. 
참고로 지정한 예외 또는 그 예외의 자식 클래스는 모두 잡을 수 있다.

### 실행흐름
- 컨트롤러를 호출한 결과 IllegalArgumentException 예외가 컨트롤러 밖으로 던져진다.
- 예외가 발생했으므로 ExceptionResolver가 작동한다. 가장 우선순위가 높은 ExceptionHandlerExceptionResolver가 실행된다.
- ExceptionHandlerExceptionResolver는 해당 컨트롤러에 IllegalArgumentException을 처리할 수 있는 @ExceptionHandler가 있는지 확인한다.
- illegalExHandle()를 실행한다. @RestController이므로 illegalExHandle()에도 @ResponseBody가 적용된다. 따라서 HTTP 컨버터가 사용되고, 응답이 다음과 같은 JSON으로 반환된다.
- @ResponseStatus(HttpStatus.BAD_REQUEST)를 지정했으므로 HTTP 상태코드 400으로 응답한다.

