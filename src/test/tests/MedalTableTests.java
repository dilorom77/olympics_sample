package tests;

import org.openqa.selenium.By;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.MedalTablePage;
import utilities.BrowserUtilities;
import utilities.ConfigurationReader;
import utilities.TestBase;

import java.util.Arrays;

import static org.testng.Assert.*;

public class MedalTableTests extends TestBase {
    private MedalTablePage medalTablePage;

    @BeforeMethod
    public void setUpPages() {
        medalTablePage = new MedalTablePage();
        driver.get(ConfigurationReader.getProperty("url"));
    }
    @AfterMethod
    public void wrapUpPages() {
        medalTablePage = null;
    }

    /**
     * Test Case 1: SORT TEST
     * 1. Go to website https://en.wikipedia.org/wiki/2016_Summer_Olympics#Medal_table
     * 2. Verify that by default the Medal table is sorted by rank.
     *      To do that you need to capture all the cells in the Rank column and check if
     *      they are in ascending order
     * 3. Click link NOC.
     * 4. Now verify that the table is now sorted by the country names. To do that you need to
     *      capture all the names in the NOC column and check if they are in ascending/alphabetical
     *      order
     * 5. Verify that Rank column is not in ascending order anymore.
     */
    @Test (priority = 1)
    public void sortTest() {
        // 1. Go to website https://en.wikipedia.org/wiki/2016_Summer_Olympics#Medal_table
            // accomplished within @BeforeMethod

        // 2. Verify that by default the Medal table is sorted by rank.
            // a. Store the elements of Rank column in a String array
            String[] initialRanking = BrowserUtilities.elementsToStringArray(
                    medalTablePage.medalTableColumnElements("Rank") );
            // b. convert the String [] into int[] to help with comparison
            int[] initialRankingNumbers = BrowserUtilities.convertStringArrayToIntArray(initialRanking);
            // c. check for the int[] to be in ascending order
            for (int i = 0; i < initialRankingNumbers.length - 1; i++)
                assertEquals(initialRankingNumbers[i + 1],initialRankingNumbers[i]+1);

        // 3. Click link NOC
        driver.findElement(By.cssSelector(".wikitable.sortable.plainrowheaders.jquery-tablesorter thead th:nth-child(2)")).click();

        // 4. Now verify that the table is now sorted by the country names.
            // a. Store the elements of NOC column in a String array
            String[] initialCountryNames = BrowserUtilities.elementsToStringArray(
                    medalTablePage.medalTableColumnElements("NOC") );
            // b. Utilizing String's .compareTo() method, verify country names are listed alphabetically
            for (int i = 0; i < initialCountryNames.length - 1; i++)
                assertTrue(initialCountryNames[i].compareTo(initialCountryNames[i + 1]) < 1);

        // 5. Verify that Rank column is not in ascending order anymore.
            // a. Store the elements of Rank column in a String array
            String[] ensuingRanking = BrowserUtilities.elementsToStringArray(
                    medalTablePage.medalTableColumnElements("Rank") );
            // b. convert the String [] into int[] to help with comparison
            int[] ensuingRankingNumbers = BrowserUtilities.convertStringArrayToIntArray(ensuingRanking);
            // c. check for the int[] to be in ascending order
            for (int i = 0; i < ensuingRankingNumbers.length - 1; i++)
                assertNotEquals(ensuingRankingNumbers[i + 1], ensuingRankingNumbers[i]+1);
    }

    /**
     * Test Case 2: THE MOST
     * 1.Write a method that returns the name of the country with the greatest number of gold medals.
     * 2.Write a method that returns the name of the country with the greatest number of silver medals.
     * 3.Write a method that returns the name of the country with the greatest number of bronze medals.
     * 4.Write a method that returns the name of the country with the greatest number of medals.
     */
    @Test (priority = 2)
    public void theMost() {
        /*
        Developed a method that takes three parameters
        1. Column heading that is to be sorted (String, must match the heading exactly)
        2. Boolean for the table to be in ascending (smallest number first) or not (highest number first)
        3. Ranking (int) of the country that is seeked for
        The method will return the String that will be the name of the country (NOC) as seen on the table
         */
        String theMostGold = medalTablePage.theMostCountry("Gold", false, 1).trim();
        String theMostSilver = medalTablePage.theMostCountry("Silver", false, 1).trim();
        String theMostBronze = medalTablePage.theMostCountry("Bronze", false, 1).trim();
        String theMostTotal = medalTablePage.theMostCountry("Total", false, 1).trim();

        assertEquals(theMostGold, "United States (USA)", "gold case");
        assertEquals(theMostSilver, "United States (USA)", "silver case");
        assertEquals(theMostBronze, "United States (USA)", "bronze case");
        assertEquals(theMostTotal, "United States (USA)", "total case");
    }

    /**
     * Test Case 3: COUNTRY BY MEDAL
     * 1.Write a method that returns a list of countries by their silver medal count. You decide the
     *  data type of the return.
     */
    @Test (priority = 3)
    public void countryByMedal() {
        /*
        Developed a method that takes two parameters:
        1. Column heading that is to be sorted (String, must match the heading exactly)
        2. Boolean for the table to be in ascending (smallest number first) or not (highest number first)
        The method will return String [] elements of which are the names of countries per criteria.
         */
        System.out.println(Arrays
                .toString(medalTablePage
                        .countriesListByCriteria("Silver", false))
                            + "\n\n\n");
    }

    /**
     * Test Case 4: GET INDEX
     * 1.Write a method that takes country name and returns the row and column number.
     *   You decide the data type of the return.
     */
    @Test (priority = 4)
    public void getIndex() {
        /*
        Developed a method that takes the country name and returns its index on the table.
        The method is flexible and should the ranking criteria change, it will find the country's
        new index amongst other countries (re: second assertion)
         */
        // due to previous test cases, this code may break. Thus, reset the ranking first
        medalTablePage.countriesListByCriteria("Rank", true);
        // then, proceed to checking the method
        assertEquals(medalTablePage.getCountryIndex("France"), 7,
                "France should be 7th amongst countries per default ranking.");

        // to put the method to test, let's call another custom method and change the country rankings
        // even though France comes 7th by default, it is 4th by Silver medals (descending)
        medalTablePage.countriesListByCriteria("Silver", false);
        assertEquals(medalTablePage.getCountryIndex("France"), 4,
                "France comes 4th from top per the number of Silver medals");
    }

    /**
     * Test Case 5: GET SUM
     * 1. Write a method that returns a list of two countries whose sum of bronze medals is 18.
     */
    @Test (priority = 5)
    public void getSum() {
        /*
        Developed a method that takes the medal type and the total number of medals requested
        and returns the names and number of medals of two countries that have the requested amount
        of total medals.
        If there are no two countries with exact total, the method will return the two countries
        that have the CLOSEST sum of medals to the requested amount.
        */
        // due to previous test cases, this code may break. Thus, reset the ranking first
        medalTablePage.countriesListByCriteria("Rank", true);

        // then, proceed to checking the method
        System.out.println(medalTablePage.getSumPerMedalCount("Bronze", 18));
    }
}
