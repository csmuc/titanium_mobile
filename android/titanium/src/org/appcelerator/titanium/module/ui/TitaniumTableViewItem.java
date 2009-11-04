/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package org.appcelerator.titanium.module.ui;

import org.appcelerator.titanium.config.TitaniumConfig;
import org.appcelerator.titanium.util.Log;
import org.appcelerator.titanium.util.TitaniumFileHelper;
import org.appcelerator.titanium.util.TitaniumUIHelper;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewAnimator;

public class TitaniumTableViewItem extends ViewAnimator implements Handler.Callback
{
	private static final String LCAT = "TitaniamTableViewItem";
	private static final boolean DBG = TitaniumConfig.LOGD;

	private static final int MSG_SHOW_VIEW_1 = 300;

	private Handler handler;

	private RowView rowView;

	class RowView extends RelativeLayout
	{
		private ImageView iconView;
		private TextView textView;
		private ImageView hasChildView;
		public boolean header;
		private LocalWebView webView;

		private Drawable hasMoreDrawable;

		private Drawable defaultBackground;
		private int defaultTextColor;

		public RowView(Context context) {
			super(context);

			setGravity(Gravity.CENTER_VERTICAL);

			iconView = new ImageView(context);
			iconView.setId(100);
			iconView.setFocusable(false);
			iconView.setFocusableInTouchMode(false);

			textView = new TextView(context);
			textView.setId(101);
			textView.setFocusable(false);
			textView.setFocusableInTouchMode(false);

			defaultBackground = getBackground();
			defaultTextColor = textView.getCurrentTextColor();

			hasChildView = new ImageView(context);
			hasChildView.setId(102);
			hasChildView.setFocusable(false);
			hasChildView.setFocusableInTouchMode(false);

			webView = new LocalWebView(context, defaultTextColor);

			LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
			params.addRule(CENTER_VERTICAL);
			params.setMargins(0, 0, 5, 0);
			addView(iconView, params);

			params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
			params.addRule(CENTER_VERTICAL);
			params.addRule(ALIGN_RIGHT);
			params.setMargins(0, 0, 7, 0);
			params.alignWithParent = true;
			addView(hasChildView, params);

			params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(CENTER_VERTICAL);
			params.addRule(RIGHT_OF, iconView.getId());
			params.addRule(LEFT_OF, hasChildView.getId());
			params.alignWithParent = true;
			addView(textView, params);

			params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(CENTER_VERTICAL);
			params.addRule(RIGHT_OF, iconView.getId());
			params.addRule(LEFT_OF, hasChildView.getId());
			params.alignWithParent = true;
			addView(webView, params);
		}

		public void setRowData(JSONObject data, int rowHeight, String fontSize, String fontWeight)
		{
			TitaniumFileHelper tfh = new TitaniumFileHelper(getContext());

			header = false;
			handler.removeMessages(MSG_SHOW_VIEW_1);

			boolean isDisplayHeader = false;
			try {
				isDisplayHeader = data.getBoolean("isDisplayHeader");
			} catch (JSONException e) {
				Log.e(LCAT, "Unable to get header flag", e);
			}

			setPadding(0, 0, 0, 0);

			if (isDisplayHeader) {
				iconView.setVisibility(View.GONE);
				textView.setVisibility(View.VISIBLE);
				hasChildView.setVisibility(View.GONE);
				webView.setVisibility(View.GONE);

				 if(data.has("header")) {
					header = true;
					setMinimumHeight(18);
					setVerticalFadingEdgeEnabled(false);
					try {
						TitaniumUIHelper.styleText(textView, "10dp", "normal");
						textView.setBackgroundColor(Color.DKGRAY);
						textView.setTextColor(Color.LTGRAY);
						textView.setPadding(4, 2, 4, 2);
						textView.setText(data.getString("header"), TextView.BufferType.NORMAL);
					} catch (JSONException e) {
						textView.setText(e.getMessage());
						Log.e(LCAT, "Error retrieving header", e);
					}
				 }
			} else {
				setVerticalFadingEdgeEnabled(true);
				if (data.has("rowHeight")) {
					try {
						rowHeight = data.getInt("rowHeight");
					} catch (JSONException e) {
						Log.w(LCAT, "Error getting rowHeight: " + e.getMessage());
					}
				}
				setMinimumHeight(rowHeight);

				if (data.has("image")) {
					try {
						String path = data.getString("image");
						Drawable d = tfh.loadDrawable(path, false);
						if (d != null) {
							BitmapDrawable b = (BitmapDrawable) d;
							if (b.getBitmap().getHeight() > rowHeight) {
								d = new BitmapDrawable(Bitmap.createScaledBitmap(b.getBitmap(), rowHeight, rowHeight, true));
							}
							iconView.setImageDrawable(d);
							iconView.setVisibility(View.VISIBLE);
						}

					} catch (JSONException e) {
						Log.e(LCAT, "Error retrieving image", e);
					}
				} else {
					iconView.setVisibility(View.GONE);
				}

				if (data.has("hasChild")) {
					try {
						if (data.getBoolean("hasChild")) {
							if(hasMoreDrawable == null) {
								hasMoreDrawable = new BitmapDrawable(getClass().getResourceAsStream("/org/appcelerator/titanium/res/drawable/btn_more.png"));
							}
							if (hasMoreDrawable != null) {
								hasChildView.setImageDrawable(hasMoreDrawable);
							}
							hasChildView.setVisibility(View.VISIBLE);
						}
					} catch (JSONException e) {
						Log.e(LCAT, "Error retrieving hasChild", e);
					}
				} else {
					hasChildView.setVisibility(View.GONE);
				}

				if (data.has("html")) {
					webView.setVisibility(View.INVISIBLE);
					textView.setVisibility(View.GONE);
					webView.setMinimumHeight(rowHeight);

					try {
						String html = data.getString("html");
						if (html != null) {
							webView.load(html, rowHeight);
						}
					} catch (JSONException e) {
						Log.e(LCAT, "Error retrieving html", e);
					}
				} else if (data.has("title")) {
					webView.setVisibility(View.GONE);
					textView.setPadding(4, 0, 4, 0);
					textView.setVisibility(View.VISIBLE);
					textView.setBackgroundDrawable(defaultBackground);
		 			textView.setTextColor(defaultTextColor);

					try {
						textView.setText(data.getString("title"), TextView.BufferType.NORMAL);
						TitaniumUIHelper.styleText(textView, data.optString("fontSize", fontSize), data.optString("fontWeight", fontWeight));
					} catch (JSONException e) {
						textView.setText(e.getMessage());
						Log.e(LCAT, "Error retrieving title", e);
					}
				}
			}

			requestLayout();
		}

		public void showWebView() {
			if (webView.getVisibility() == View.INVISIBLE) {
				webView.setVisibility(View.VISIBLE);
			}
		}
	}

	class LocalWebView extends WebView
	{
		///public int rowHeight;

		private String htmlPrefix;
		private String htmlPostfix;
		private int rowHeight;

		public LocalWebView(Context context, int defaultTextColor)
		{
			super(context);

			this.setFocusable(false);
			this.setFocusableInTouchMode(false);
			this.setClickable(false);

			WebSettings settings = getSettings();
			settings.setLoadsImagesAutomatically(true);
			settings.setSupportMultipleWindows(false);
			settings.setSupportZoom(false);
			settings.setJavaScriptCanOpenWindowsAutomatically(false);
			settings.setJavaScriptEnabled(false);

			setScrollContainer(false);
			setHorizontalScrollBarEnabled(false);
			setVerticalScrollBarEnabled(false);

			setBackgroundColor(Color.TRANSPARENT);
			setId(101);

			StringBuilder sb = new StringBuilder();
			sb.append("<html><body style='color:rgba(").append(Color.red(defaultTextColor)).append(",")
				.append(Color.green(defaultTextColor)).append(",")
				.append(Color.blue(defaultTextColor)).append(",")
				.append(Color.alpha(defaultTextColor))
				.append("); '>")
				;
			htmlPrefix = sb.toString();
			htmlPostfix = "</body></html>";

			setWebViewClient(new WebViewClient() {

				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);

					handler.sendEmptyMessageDelayed(MSG_SHOW_VIEW_1, 10);
				}

			});
		}

		public void load(String html, int rowHeight)
		{
			this.rowHeight = rowHeight;
			StringBuilder sb = new StringBuilder();
			sb.append(htmlPrefix).append(html).append(htmlPostfix);
			//sb.append(html);
			loadDataWithBaseURL("file:///android_asset/Resources/", sb.toString(), "text/html", "UTF-8", null);
		}

		private String modeString(int mode) {
			switch (mode) {
			case MeasureSpec.AT_MOST : return "AT_MOST";
			case MeasureSpec.EXACTLY : return "EXACTLY";
			case MeasureSpec.UNSPECIFIED : return "UNSPECIFIED";
			default : return "DEFAULT";
			}
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			if (false) {
				int wMode = MeasureSpec.getMode(widthMeasureSpec);
				int wSize = MeasureSpec.getSize(widthMeasureSpec);
				int hMode = MeasureSpec.getMode(heightMeasureSpec);
				int hSize = MeasureSpec.getSize(heightMeasureSpec);

				Log.e(LCAT, "MEASURE: " + modeString(wMode) + " " + wSize + ", " + modeString(hMode) + " " + hSize);
			}
			setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), rowHeight);
		}
	}

	public TitaniumTableViewItem(Context context)
	{
		super(context);

		this.handler = new Handler(this);
		rowView = new RowView(context);
		this.addView(rowView, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		setDisplayedChild(0);

		//Animation a = new AlphaAnimation(0.0f, 1.0f);
		//a.setDuration(150);
		//setInAnimation(a);
	}

	public void setRowData(JSONObject data, int rowHeight, String fontSize, String fontWeight) {
		rowView.setRowData(data, rowHeight, fontSize, fontWeight);
	}

	public boolean handleMessage(Message msg)
	{
		if (msg.what == MSG_SHOW_VIEW_1) {
			rowView.showWebView();
			//requestLayout();
			invalidate();
			return true;
		}
		return false;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		Log.e(LCAT, "TTVI d1");
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean dispatchKeyEventPreIme(KeyEvent event) {
		Log.e(LCAT, "TTVI d2");
		return super.dispatchKeyEventPreIme(event);
	}

	public boolean isHeader() {
		return rowView.header;
	}
}
