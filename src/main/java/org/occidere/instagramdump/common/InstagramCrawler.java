package org.occidere.instagramdump.common;

/**
 * @author occidere
 * @since 2018-12-31
 * Blog: https://blog.naver.com/occidere
 * Github: https://github.com/occidere
 */

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.occidere.instagramdump.domain.InstagramPhoto;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class InstagramCrawler {
	private Logger log = LoggerFactory.getLogger(InstagramCrawler.class);
	private Logger errorLog = LoggerFactory.getLogger("errorLogger");

	public static final String INSTAGRAM_URL = "https://www.instagram.com";
	private static final DateTimeFormatter INSTAGRAM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	@Value("${chromedriver.path}")
	private String chromedriverPath;
	@Setter
	private int range;
	@Setter
	private String url;

	public List<InstagramPhoto> getResult() {
		return getResultInRange(url);
	}

	/**
	 * url 에 해당하는 인스타그램과 연결된 상태의 WebDriver 객체를 반환한다
	 *
	 * @param url 연결할 인스타그램 url
	 * @return url 에 해당하는 인스타그램과 연결된 상태의 WebDriver 객체
	 */
	private WebDriver getChromeDriver(String url) { // url: https://www.instagram.com/wm_ohmygirl/?hl=ko
		System.setProperty("webdriver.chrome.driver", chromedriverPath);

		ChromeOptions chromeOptions = new ChromeOptions();
		chromeOptions.addArguments("headless");
		chromeOptions.addArguments("--window-size=1920,1080"); // 화면이 너무 작으면 > 클릭 버튼이 표시 안되서 에러남

		WebDriver driver = new ChromeDriver(chromeOptions);
		driver.get(url); // https://www.instagram.com/wm_ohmygirl/?hl=ko

		return driver;
	}

	/**
	 * url 로 부터 지정한 {@code range} 내의 사진들을 모두 가져온다.
	 *
	 * @param url 사진을 가져올 url
	 * @return 지정한 range 에 포함되는 url의 사진들이 담긴 List
	 */
	private List<InstagramPhoto> getResultInRange(String url) {
		if (StringUtils.isBlank(url)) {
			errorLog.error("Empty URL!");
			throw new RuntimeException("EmptyURLException");
		}

		List<InstagramPhoto> instagramPhotos = new ArrayList<>();
		WebDriver driver = getChromeDriver(url);

		try {
			log.info("URL: {}", url);

			// TODO: 컬렉션들 OOM 발생 방지 대책 마련
			Set<String> postUrlSet = new HashSet<>();

			boolean inRange = true;
			int retryCount = 5;

			// 더 이상 사진을 못 가져왔거나, 날짜 범위를 초과하기 전 까지 계속 스크롤 내리면서 크롤링
			while (retryCount > 0 && inRange) {
				// 포스팅 주소들 가져옴
				List<String> postUrlList = getPostList(getHtml(driver, "section"));

				// 이미 가져왔던 주소들이랑 중복 제외한 새로운 주소만 모음
				List<String> uniqueUrlList = postUrlList.stream()
						.filter(postUrl -> !postUrlSet.contains(postUrl))
						.collect(Collectors.toList());

				// set에 신규 주소 추가
				postUrlSet.addAll(uniqueUrlList);

				// 포스팅 주소를 새로 못 받아왔으면 스크롤 안하고 잠시 대기 후 재시도
				if (CollectionUtils.isEmpty(uniqueUrlList)) {
					retryCount--;
					log.info("Retry remains: {}", retryCount);
					Thread.sleep(1500);
				} else {
					scrollDown(driver, 1500); // 스크롤 내리고 이미지 로딩 위해 1.5초 대기
				}

				for (String postUrl : uniqueUrlList) {
					InstagramPhoto photo = getInstagramPhoto(postUrl);

					String date = photo.getDate();

					if (isInRange(date)) {
						instagramPhotos.add(photo);
					} else {
						// 날짜 범위 넘은 게시물이 나왔으면 더 이상 크롤링 안해도 됨
						inRange = false;
						break; // TODO: 여기서 멈춰도 되는지 고려
					}
				}
			}

		} catch (Exception e) {
			errorLog.error("InstagramImageFetchError", e);
		} finally {
			driver.quit();
		}

		return instagramPhotos;
	}

	/**
	 * 인스타 포스팅 주소에 담긴 포스팅 시간과 이미지들의 url을 Map 에 담아 반환한다.
	 *
	 * @param postUrl 인스타 포스팅 주소
	 * @return 포스팅 시간과 이미지들의 url이 담긴 Map
	 */
	private InstagramPhoto getInstagramPhoto(String postUrl) throws Exception {
		log.info("Post URL: {}", postUrl);

		Set<String> urlSet = new HashSet<>();
		WebDriver driver = getChromeDriver(postUrl); // 다음사진 보기용 오른쪽 클릭을 하기 위한 WebElement

		String date = "";
		String author = "";
		String content = "";

		InstagramPhoto photo = new InstagramPhoto();
		photo.setPageUrl(postUrl);

		do {
			Document doc = Jsoup.parse(getHtml(driver, "section")); //openConnection(postUrl, Method.GET, null, null);
			Element article = doc.getElementsByTag("article").get(0);

			// ISO 8601 (2018-09-27T14:19:44.000Z)
			// TODO 날짜 값은 time 태그의 datetime 어트리뷰트에 있음
			if (StringUtils.isBlank(date)) {
				date = LocalDateTime.parse(
						article.selectFirst("time").attr("datetime"), INSTAGRAM_FORMATTER)
						.plusHours(9) // 인스타는 UTC+0 을 사용하므로 KST 로 강제 고정
						.format(FORMATTER);
				photo.setDate(date);
				log.info("Post Date: {}", date);
			}

			// TODO 작성자, 본문 내용은 C4VMK 에 있음
			if (StringUtils.isBlank(content) || StringUtils.isBlank(author)) {
				Element authorArea = article.selectFirst("div.C4VMK");

				content = authorArea.selectFirst("span").text().replaceAll("[\\n\\r]", "");
				photo.setContent(content);
				log.info("Content: {}", content);

				author = authorArea.selectFirst("h2._6lAjh").text();
				photo.setAuthor(author);
				log.info("Author: {}", author);
			}

			// TODO 사진들은 FFVAD 에 있음
			urlSet.addAll(new HashSet<>(article.select(".FFVAD").eachAttr("src")));
		} while (clickRightAndHasMore(driver));
		driver.quit();

		photo.setOriginImageUrls(new ArrayList<>(urlSet));

		log.info("Image Count: {}", urlSet.size());
		log.info(photo.toString());

		return photo;
	}

	/**
	 * 여러 사진이 있는 경우 > 버튼을 클릭하여 다음 사진을 가져온다.
	 * > 버튼이 있어서 클릭에 성공하면 true, 버튼이 없어서 클릭에 실패하면 false를 반환한다.
	 *
	 * @param driver postUrl과 연결중인 driver 객체
	 * @return > 버튼이 있어서 클릭 성공 시 true, 없어서 클릭 실패 시 false
	 */
	private boolean clickRightAndHasMore(WebDriver driver) {
		//  _6CZji <- button
		//    coreSpriteRightChevron <- div
		try {
			WebElement rightButton = driver.findElement(By.cssSelector("button[class='  _6CZji']")); // 인스타 다음 사진 보기 버튼
			rightButton.click();

			// 위 기능으로 버튼 클릭이 동작 안할 시 아래 방법 고려
//       Actions actions = new Actions(driver);
//       actions.moveToElement(rightButton).click().perform();

			log.info("Click next button!");

			return true;
		} catch (Exception e) {
			// 다음 사진 보기 버튼 없으면 에러
//       e.printStackTrace();
			return false;
		}
	}

	protected boolean isInRange(String date) {
		LocalDate postDate = LocalDateTime.parse(date, FORMATTER).toLocalDate(); // yyyyMMddHHmmss
		long days = Math.abs(ChronoUnit.DAYS.between(postDate, LocalDate.now()));
		return days <= range;
	}

	/**
	 * 페이지 스크롤 다운
	 *
	 * @param driver
	 * @param waitMs 스크롤 다운하고 이미지 로딩할 동안 기다릴 대기시간
	 */
	private void scrollDown(WebDriver driver, int waitMs) {
		try {
			((JavascriptExecutor) driver).executeScript("window.scrollBy(0,10000);"); // TODO: 스크롤 값 조절
			Thread.sleep(waitMs);
		} catch (Exception e) {
			errorLog.error("ScrollDownError", e);
		}
	}

	/**
	 * section 태그 부분의 html 코드를 가져온다
	 *
	 * @param driver
	 * @return section 태그 부분의 html 코드
	 */
	private String getHtml(WebDriver driver, String tagName) {
		return ((ChromeDriver) driver).findElementByTagName(tagName)
				.getAttribute("outerHTML");
	}

	/**
	 * html 태그로 부터 인스타그램 포스트의 주소(resource 부분)를 가져온다.
	 * ex) /p/BlvUYgmglwz/
	 *
	 * @param html 인스타 포스팅 주소가 담긴 html 소스
	 * @return 인스타 포스팅 주소 (resource 부분)
	 */
	private List<String> getPostList(String html) {
		// TODO: 포스팅 주소가 담긴 부분의 클래스 이름 언제 바뀔 지 모르니 예의주시할 것
		return Jsoup.parse(html).getElementsByClass("v1Nh3 kIKUG  _bz0w")
				.stream()
				.map(x -> x.getElementsByTag("a"))
				.map(e -> INSTAGRAM_URL + e.attr("href"))
				.collect(Collectors.toList());
	}

}
