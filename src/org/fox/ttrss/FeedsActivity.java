package org.fox.ttrss;

import java.util.HashMap;

import org.fox.ttrss.types.Article;
import org.fox.ttrss.types.ArticleList;
import org.fox.ttrss.types.Feed;
import org.fox.ttrss.types.FeedCategory;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

public class FeedsActivity extends OnlineActivity implements HeadlinesEventListener, ArticleEventListener {
	private final String TAG = this.getClass().getSimpleName();
	
	protected SharedPreferences m_prefs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		m_prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		if (m_prefs.getString("theme", "THEME_DARK").equals("THEME_DARK")) {
			setTheme(R.style.DarkTheme);
		} else {
			setTheme(R.style.LightTheme);
		}

		super.onCreate(savedInstanceState);

		setContentView(R.layout.feeds);
		
		setSmallScreen(findViewById(R.id.headlines_fragment) == null); 
		
		if (savedInstanceState == null) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

			if (m_prefs.getBoolean("enable_cats", false)) {
				ft.replace(R.id.feeds_fragment, new FeedCategoriesFragment(), FRAG_CATS);				
			} else {
				ft.replace(R.id.feeds_fragment, new FeedsFragment(), FRAG_FEEDS);
			}
			
			ft.commit();
		} /* else if (isSmallScreen()) {
			Fragment frag = getSupportFragmentManager().findFragmentByTag(FRAG_ARTICLE);
			if (frag != null) {
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.remove(frag);
				ft.commit();
			} 
		} */
	}
	
	@Override
	protected void initMenu() {
		super.initMenu();
		
		Log.d(TAG, "initMenu: " + m_menu);
		
		if (m_menu != null && m_sessionId != null) {
			Fragment ff = getSupportFragmentManager().findFragmentByTag(FRAG_FEEDS);
			Fragment cf = getSupportFragmentManager().findFragmentByTag(FRAG_CATS);
			Fragment af = getSupportFragmentManager().findFragmentByTag(FRAG_ARTICLE);

			HeadlinesFragment hf = (HeadlinesFragment)getSupportFragmentManager().findFragmentByTag(FRAG_HEADLINES);
			
			Log.d(TAG, "ff/cf/af/hf " + ff + " " + cf + " " + af + " " + hf);
			
			m_menu.setGroupVisible(R.id.menu_group_feeds, ff != null || cf != null);
			
			m_menu.setGroupVisible(R.id.menu_group_article, af != null);

			m_menu.setGroupVisible(R.id.menu_group_headlines, hf != null && hf.getSelectedArticles().size() == 0);
			m_menu.setGroupVisible(R.id.menu_group_headlines_selection, hf != null && hf.getSelectedArticles().size() != 0);
			
			MenuItem item = m_menu.findItem(R.id.show_feeds);

			if (getUnreadOnly()) {
				item.setTitle(R.string.menu_all_feeds);
			} else {
				item.setTitle(R.string.menu_unread_feeds);
			}

		}		
	}
	
	
	public void onFeedSelected(Feed feed) {
		FragmentTransaction ft = getSupportFragmentManager()
				.beginTransaction();
		
		HeadlinesFragment hf = new HeadlinesFragment(feed);

		if (isSmallScreen()) {
			ft.replace(R.id.feeds_fragment, hf, FRAG_HEADLINES);
			ft.addToBackStack(null);
		} else {
			ft.replace(R.id.headlines_fragment, hf, FRAG_HEADLINES);
		}
		ft.commit();		
	}
	
	public void onCatSelected(FeedCategory cat, boolean openAsFeed) {

		FragmentTransaction ft = getSupportFragmentManager()
				.beginTransaction();
		
		if (!openAsFeed) {
			FeedsFragment ff = new FeedsFragment(cat);

			ft.replace(R.id.feeds_fragment, ff, FRAG_FEEDS);
		} else {
			Feed feed = new Feed(cat.id, cat.title, true);
			onFeedSelected(feed);
		}

		ft.addToBackStack(null);

		ft.commit();
	}
	
	public void onCatSelected(FeedCategory cat) {
		onCatSelected(cat, m_prefs.getBoolean("browse_cats_like_feeds", false));		
	}
	
	private void refresh() {
		FeedCategoriesFragment cf = (FeedCategoriesFragment) getSupportFragmentManager().findFragmentByTag(FRAG_CATS);
		
		if (cf != null) {
			cf.refresh(false);
		}

		FeedsFragment ff = (FeedsFragment) getSupportFragmentManager().findFragmentByTag(FRAG_FEEDS);
		
		if (ff != null) {
			ff.refresh(false);
		}

		HeadlinesFragment hf = (HeadlinesFragment) getSupportFragmentManager().findFragmentByTag(FRAG_HEADLINES);
		
		if (hf != null) {
			hf.refresh(false);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.show_feeds:
			m_unreadOnly = !m_unreadOnly;
			initMenu();
			refresh();
			return true;
		case R.id.update_feeds:
			refresh();
			return true;
		default:
			Log.d(TAG, "onOptionsItemSelected, unhandled id=" + item.getItemId());
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void loginSuccess() {
		setLoadingStatus(R.string.blank, false);
		findViewById(R.id.loading_container).setVisibility(View.GONE);
		initMenu();
	}
	
	@Override
	public void onSaveInstanceState(Bundle out) {
		super.onSaveInstanceState(out);
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onArticleListSelectionChange(ArticleList m_selectedArticles) {
		initMenu();		
	}

	public void onArticleSelected(Article article, boolean open) {
		if (article.unread) {
			article.unread = false;
			saveArticleUnread(article);
		}
		
		if (open) {			
			if (isSmallScreen()) {
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				
				HeadlinesFragment hf = (HeadlinesFragment) getSupportFragmentManager().findFragmentByTag(FRAG_HEADLINES);
				
				Fragment frag = new ArticlePager(article, hf.getAllArticles());

				ft.replace(R.id.feeds_fragment, frag, FRAG_ARTICLE);
				ft.addToBackStack(null);
				
				ft.commit();
			} else {
				Intent intent = new Intent(FeedsActivity.this, HeadlinesActivity.class);
				intent.putExtra("sessionId", m_sessionId);
				intent.putExtra("apiLevel", m_apiLevel);
				
				HeadlinesFragment hf = (HeadlinesFragment)getSupportFragmentManager().findFragmentByTag(FRAG_HEADLINES);
				
				intent.putExtra("feed", hf.getFeed());
				intent.putParcelableArrayListExtra("articles", hf.getAllArticles());
				intent.putExtra("activeArticle", article);
				intent.putExtra("article", article);
				
				//intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		 	   
				startActivityForResult(intent, 0);
			}
		} else {
			/* HeadlinesFragment hf = (HeadlinesFragment) getSupportFragmentManager().findFragmentByTag(FRAG_HEADLINES);
			if (hf != null) hf.setActiveArticle(article); */
		}
	}

	@Override
	public void onArticleSelected(Article article) {
		onArticleSelected(article, true);		
	}

}