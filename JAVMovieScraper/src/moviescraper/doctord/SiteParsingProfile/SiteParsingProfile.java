package moviescraper.doctord.SiteParsingProfile;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import moviescraper.doctord.Thumb;
import moviescraper.doctord.dataitem.Actor;
import moviescraper.doctord.dataitem.Director;
import moviescraper.doctord.dataitem.Genre;
import moviescraper.doctord.dataitem.ID;
import moviescraper.doctord.dataitem.MPAARating;
import moviescraper.doctord.dataitem.OriginalTitle;
import moviescraper.doctord.dataitem.Outline;
import moviescraper.doctord.dataitem.Plot;
import moviescraper.doctord.dataitem.Rating;
import moviescraper.doctord.dataitem.Set;
import moviescraper.doctord.dataitem.SortTitle;
import moviescraper.doctord.dataitem.Studio;
import moviescraper.doctord.dataitem.Tagline;
import moviescraper.doctord.dataitem.Title;
import moviescraper.doctord.dataitem.Top250;
import moviescraper.doctord.dataitem.Votes;
import moviescraper.doctord.dataitem.Year;

public abstract class SiteParsingProfile {
	
	public enum Language { ENGLISH, JAPANESE }

	public Document document; // the base page to start parsing from
	
	public String overrideURL;

	public String getOverrideURL() {
		return overrideURL;
	}

	public void setOverrideURL(String overrideURL) {
		this.overrideURL = overrideURL;
	}

	public SiteParsingProfile(Document document) {
		this.document = document;
		overrideURL = null;
	}
	
	public SiteParsingProfile(){}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}
	
	
	//Gets the ID number from the file and considers stripped out multipart file identifiers like CD1, CD2, etc
	//The ID number needs to be the last word in the filename or the next to the last word in the file name if the file name
	//ends with something like CD1 or Disc 1
	//So this filename "My Movie ABC-123 CD1" would return the id as ABC-123
	//This filename "My Movie ABC-123" would return the id as ABC-123
	public static String findIDTagFromFile(File file)
	{
		String fileNameNoExtension = FilenameUtils.removeExtension(file.getName());
		String fileNameNoExtensionNoDiscNumber = stripDiscNumber(fileNameNoExtension);
		String[] splitFileName = fileNameNoExtensionNoDiscNumber.split(" ");
		String lastWord = splitFileName[splitFileName.length-1];
		
		//Some people like to enclose the ID number in parenthesis or brackets like this (ABC-123) or this [ABC-123] so this gets rid of that
		//TODO: Maybe consolidate these lines of code using a single REGEX?
		lastWord = lastWord.replace("(","");
		lastWord = lastWord.replace(")","");
		lastWord = lastWord.replace("[","");
		lastWord = lastWord.replace("]","");
		return lastWord;
	}

	public static String stripDiscNumber(String fileNameNoExtension) {
		//replace <cd/dvd/part/pt/disk/disc/d> <0-N>  (case insensitive) with empty
		String discNumberStripped =  fileNameNoExtension.replaceAll("(?i)[ _.]+(?:cd|dvd|p(?:ar)?t|dis[ck]|d)[ _.]*[0-9]+$", "");
		//replace <cd/dvd/part/pt/disk/disc/d> <a-d> (case insensitive) with empty
		discNumberStripped = discNumberStripped.replaceAll("(?i)[ _.]+(?:cd|dvd|p(?:ar)?t|dis[ck]|d)[ _.]*[a-d]$","");
		return discNumberStripped.trim();
	}

	public abstract Title scrapeTitle();

	public abstract OriginalTitle scrapeOriginalTitle();

	public abstract SortTitle scrapeSortTitle();

	public abstract Set scrapeSet();

	public abstract Rating scrapeRating();

	public abstract Year scrapeYear();

	public abstract Top250 scrapeTop250();

	public abstract Votes scrapeVotes();

	public abstract Outline scrapeOutline();

	public abstract Plot scrapePlot();

	public abstract Tagline scrapeTagline();

	public abstract moviescraper.doctord.dataitem.Runtime scrapeRuntime();

	public abstract Thumb[] scrapePosters();

	public abstract Thumb[] scrapeFanart();

	public abstract MPAARating scrapeMPAA();

	public abstract ID scrapeID();

	public abstract ArrayList<Genre> scrapeGenres();

	public abstract ArrayList<Actor> scrapeActors();

	public abstract ArrayList<Director> scrapeDirectors();

	public abstract Studio scrapeStudio();
	
	public  abstract String createSearchString(File file);
	
	public abstract String[] getSearchResults(String searchString) throws IOException;
	
	public String [] getLinksFromGoogle(String searchQuery, String site)
	{
		Document doc;
		ArrayList<String> linksToReturn = new ArrayList<String>();
	    try{
	    	String encodingScheme = "UTF-8";
	    	String queryToEncode = "site:" + site + " " + searchQuery;
	    	String encodedSearchQuery = URLEncoder.encode(queryToEncode, encodingScheme);
	        doc = Jsoup.connect("https://www.google.com/search?q="+encodedSearchQuery).userAgent("Mozilla").ignoreHttpErrors(true).timeout(0).get();
	        Elements links = doc.select("li[class=g]");
	        for (Element link : links) {	            
	            Elements hrefs = link.select("h3.r a");
	            String href = hrefs.attr("href");
	            href = URLDecoder.decode(href, encodingScheme);
	            href = href.replaceFirst(Pattern.quote("/url?q="), "");
	            //remove some junk referrer stuff
	            int startIndexToRemove = href.indexOf("&sa=");
	            if (startIndexToRemove > -1)
	            	href = href.substring(0, startIndexToRemove);
	            linksToReturn.add(href);
	        }
	        return linksToReturn.toArray(new String[linksToReturn.size()]);
	    }
	    catch (IOException e) {
	        e.printStackTrace();
	        return linksToReturn.toArray(new String[linksToReturn.size()]);
	    }
	}
}
