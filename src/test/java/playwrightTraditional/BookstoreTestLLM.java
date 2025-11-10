package playwrightTraditional;

// Playwright imports
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

// JUnit 5 imports
import org.junit.jupiter.api.*;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

// Import for Paths (needed for video)
import java.nio.file.Paths;

public class BookstoreTestLLM {

    static Playwright playwright;
    static Browser browser;

    BrowserContext context;
    Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)); // Set to false for debugging, true for CI
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        // Video recording setup
        context = browser.newContext(new Browser.NewContextOptions()
                .setRecordVideoDir(Paths.get("videos/")) 
                .setRecordVideoSize(1280, 720)); 

        // Clear cache and cookies
        context.clearCookies();
        context.clearPermissions();

        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    @Test
    void testEarbudsSearch() {
        // Navigate to the DePaul bookstore
        page.navigate("https://depaul.bncollege.com/");

        // Search for "earbuds"
        Locator searchBox = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Search"));
        searchBox.fill("earbuds");
        searchBox.press("Enter");

        // Filter by Brand "JBL"
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("brand")).click();
        page.getByRole(AriaRole.LISTITEM)
                .filter(new Locator.FilterOptions().setHasText("brand JBL"))
                .getByRole(AriaRole.IMG)
                .click();

        // Filter by Color "Black"
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Color")).click();
        page.getByRole(AriaRole.LISTITEM)
                .filter(new Locator.FilterOptions().setHasText("Color Black"))
                .locator("svg")
                .first()
                .click();

        // Filter by Price "Over $50"
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Price")).click();
        page.locator("#facet-price svg").nth(2).click(); // This should be the "Over $50" option

        // Click the "JBL Quantum True Wireless" item link
        page.getByTitle("JBL Quantum True Wireless").first().click();

        // Assert the product name
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("JBL Quantum True Wireless")).first()).isVisible();
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("JBL Quantum True Wireless")).first()).containsText("JBL Quantum True Wireless");

        // Assert the SKU number (668972707)
        assertThat(page.getByText("668972707").nth(1)).isVisible();

        // Assert the price ($164.98)
        assertThat(page.getByText("$164.98").first()).isVisible();
        
        // Additional assertions to verify product details
        assertThat(page.locator(".description")).containsText("Adaptive noise cancelling");

        // Add 1 to the Cart
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add to cart")).click();

        // Assert that the cart icon shows "1 Items"
        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Cart 1 items"))).isVisible();
    }
}
