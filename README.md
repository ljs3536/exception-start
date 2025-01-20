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


