package pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import utilities.BrowserUtilities;
import utilities.ConfigurationReader;
import utilities.Driver;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class MedalTablePage {
    public MedalTablePage() {
        PageFactory.initElements(Driver.getDriver(), this);
    }

    private String URL = ConfigurationReader.getProperty("url");
    private String title = "2016 Summer Olympics - Wikipedia";

    @FindBy (css = ".wikitable.sortable.plainrowheaders.jquery-tablesorter")
    public WebElement medalTable;

    @FindAll(@FindBy (css = ".wikitable.sortable.plainrowheaders.jquery-tablesorter thead th"))
    public List<WebElement> medalTableHeadings;

    @FindAll(@FindBy (css = ".wikitable.sortable.plainrowheaders.jquery-tablesorter tbody tr"))
    public List<WebElement> medalTableRows;

    @FindBy (css = ".wikitable.sortable.plainrowheaders.jquery-tablesorter thead th:nth-child(2)")
    public WebElement tableHeaderNOCClickable;

    /**
     * Generates the List<WebElement> per the column heading passed as String.
     * Throws NoSuchElementException if no such heading exists in the table.
     *
     * @param columnHeading String representation of column heading
     * @return              List<WebElement> of given heading
     */
    public List<WebElement> medalTableColumnElements(String columnHeading) throws NoSuchElementException {
        String[] strArr = BrowserUtilities.elementsToStringArray(medalTableHeadings);
        int index = 0;
        boolean found = false;
        for (int i = 0; i < strArr.length; i++) {
            if (strArr[i].equals(columnHeading)) {
                index = i+1;
                found = true;
                break;
            }
        }
        if (!found || index <= 0 || index >= 7) throw new NoSuchElementException("The table doesn't contain " + columnHeading + " as heading.");

        String retCSS = ".wikitable.sortable.plainrowheaders.jquery-tablesorter tbody td:nth-child(" + index;

        if (index == 2) retCSS = ".wikitable.sortable.plainrowheaders.jquery-tablesorter tbody th";

        return BrowserUtilities.cssToList(retCSS);
    }

    /**
     * Finds and returns the name of the country based on the parameters.
     *
     * @param criteriaColumnHeading The String of column heading that the return is to be based on
     * @param ascending             If true, table goes from the lowest to the highest, otherwise if false
     * @param ranking               Define the ranking of the country being requested
     * @return                      String of the table content under NOC column per provided criteria
     */
    public String theMostCountry(String criteriaColumnHeading, boolean ascending, int ranking) {
        WebElement headerToClick = null;
        for (WebElement each : medalTableHeadings)
            if (each.getText().equals(criteriaColumnHeading))
                headerToClick = each;


        headerToClick.click();
        if (!ascending)
            headerToClick.click();

        List<WebElement> countries = medalTableColumnElements("NOC");

        return countries.get(ranking - 1).getText();
    }

    /**
     * Finds and returns the names of the countries based on the parameters.
     *
     * @param criteriaColumnHeading The String of column heading that the return array is based on
     * @param ascending             If true, table goes from the lowest to the highest, otherwise if false
     * @return                      String array of table content under NOC column per provided criteria
     */
    public String[] countriesListByCriteria(String criteriaColumnHeading, boolean ascending) {
        List<WebElement> countries = medalTableColumnElements("NOC");
        String[] retArr = new String[countries.size() - 1];

        for (int i = 0; i < retArr.length; i++) {
            retArr[i] = theMostCountry(criteriaColumnHeading, ascending, i + 1);
            if (ascending)
                ascending = false;
        }

        return retArr;
    }

    /**
     * Finds and returns the ranking of the country on the table, compared to other countries.
     *
     * @param countryName   String representation of the country name, can be a partial match
     * @return              int representation of the country's ranking compared to others
     */
    public int getCountryIndex(String countryName) {
        List<WebElement> countries = medalTableColumnElements("NOC");

        for (int i = 0; i < countries.size(); i++)
            if (countries.get(i).getText().contains(countryName))
                return i+1;

        return -1;
    }

    /**
     * Generates a 2-dimensional String[] that stores the country and their medal listings in ascending
     * order. Each element of the String[][] is another String[] that holds country name as the first
     * element and number of medals of that country as the second.
     *
     * @param criteria  String representation of the medal type
     * @return          2D String[] that holds country names and respective number of medals
     */
    public String[][] getMedalPerCountry(String criteria) {
        String[] countryNames = countriesListByCriteria(criteria, true);
        String[] medalStrings = BrowserUtilities.elementsToStringArray(
                medalTableColumnElements(criteria));

        String[] medalsCount = Arrays.copyOfRange(medalStrings, 0, countryNames.length);

        String[][] retArr = new String[countryNames.length][2];
        for (int i = 0; i < retArr.length; i++) {
            retArr[i][0] = countryNames[i].trim();
            retArr[i][1] = medalsCount[i];
        }

        return retArr;
    }

    /**
     * Generates a String message based on the medal criteria and how many total medals are requested.
     * The method will return the country information that has the lowest amount of medals should the
     * sum requested is less than that country's medal achievements.
     * It will try to return the String representation of 2 country information with their respective
     * medal counts that sum up exactly to the amount requested.
     * However, should the method fail to do as stated above, it will return the information of two
     * countries that have the closest amount of total medals to the requested sum.
     *
     * @param criteria          String representation of the type of medal being requested
     * @param sumRequested      int number of total medals being requested
     * @return                  String return of information on the output
     */
    public String getSumPerMedalCount(String criteria, int sumRequested) {
        String[][] initData = getMedalPerCountry(criteria);
        int[] workArr = BrowserUtilities.convert2DArray2ndArrayIntoIntArray(initData);
        int sum = -1;
        int closestMatchI = 0, closestMatchJ = 0;
        String retStr = "You requested " + criteria + " medals to the total count of " + sumRequested + ". We found: ";

        if (sumRequested < workArr[0])
            return retStr + "the minimum medal count is " + workArr[0] + ". You requested " + sumRequested;

        if (sumRequested == workArr[0])
            return retStr + Arrays.toString(initData[0]);

        for (int i = 0; i < workArr.length - 1; i++)
            for (int j = i + 1; j < workArr.length; j++)
                if (workArr[i] + workArr[j] == sumRequested) {
                    return retStr + "\n" + Arrays.toString(initData[i]) + "\n" + Arrays.toString(initData[j]);
                }else {
                    if ( Math.abs(sum - sumRequested) > Math.abs(workArr[i] + workArr[j] - sumRequested) ) {
                        sum = workArr[i] + workArr[j];
                        closestMatchI = i;
                        closestMatchJ = j;
                    }
                }

        return retStr + " no exact matches. \nHowever, these closest total of two countries is " + sum + " of " +
                "\n\t\t" + Arrays.toString(initData[closestMatchI]) + "\n\t\t" + Arrays.toString(initData[closestMatchJ]);
    }

    public String getURL() { return URL; }

    public String getTitle() { return title; }
}
