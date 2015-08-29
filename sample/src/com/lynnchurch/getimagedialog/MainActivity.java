package com.lynnchurch.getimagedialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.lynnchurch.getimagedialog.GetImageDialog;

public class MainActivity extends Activity
{
	private ImageView mBtn;
	private GetImageDialog mDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mBtn = (ImageView) findViewById(R.id.btn);
		mDialog = new GetImageDialog(this);
		// 设置输出圆形框图像，考虑到头像有时要用
		mDialog.setCircleImage(true);
		// 设置剪裁比例
//		mDialog.setCropRatio(1, 1);
		// 设置输出图像的尺寸大小
//		mDialog.setOutputImageSize(144, 144);
		mBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				mDialog.show();
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Bitmap bitmap = mDialog.getBitmap(requestCode, resultCode, data);
		if (null != bitmap)
		{
			mBtn.setImageBitmap(bitmap);
		}
	}

}
