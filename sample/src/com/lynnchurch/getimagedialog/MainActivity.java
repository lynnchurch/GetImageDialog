package com.lynnchurch.getimagedialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.lynnchurch.getimagedialog.GetImageDialog;

public class MainActivity extends Activity
{
	public static final int FROM_AVATAR = 0x1;
	public static final int FROM_CARD = 0x2;
	private ImageView iv_avatar;
	private ImageView iv_card;
	private GetImageDialog mDialog;
	private int mFrom;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		iv_avatar = (ImageView) findViewById(R.id.iv_avatar);
		iv_card = (ImageView) findViewById(R.id.iv_card);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Bitmap bitmap = mDialog.getBitmap(requestCode, resultCode, data);
		if (null != bitmap)
		{
			switch (mFrom)
			{
			case FROM_AVATAR:
				iv_avatar.setImageBitmap(bitmap);
				break;
			case FROM_CARD:
				iv_card.setImageBitmap(bitmap);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * 获取头像
	 * 
	 * @param v
	 */
	public void getAvatar(View v)
	{
		mFrom = FROM_AVATAR;
		mDialog = new GetImageDialog(this);
		// 设置输出圆形框图像，考虑到头像有时要用
		mDialog.setCircleImage(true);
		mDialog.show();
	}

	/**
	 * 获取卡片照
	 * 
	 * @param v
	 */
	public void getCard(View v)
	{
		mFrom = FROM_CARD;
		mDialog = new GetImageDialog(this);
		// 设置图像输出尺寸
		mDialog.setOutputImageSize(326, 184);
		// 设置截图框比例
		mDialog.setCropRatio(86, 54);
		mDialog.show();
	}

}
