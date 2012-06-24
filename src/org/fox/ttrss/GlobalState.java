package org.fox.ttrss;

import org.fox.ttrss.types.Article;
import org.fox.ttrss.types.ArticleList;
import org.fox.ttrss.types.FeedCategory;
import org.fox.ttrss.types.FeedCategoryList;

import android.app.Application;

public class GlobalState extends Application {
	public ArticleList m_articles;
	public Article m_selectedArticle;
	public FeedCategoryList m_categories;
	public FeedCategory m_selectedCategory;	
}
