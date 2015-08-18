package com.lynnchurch.getimagedialog;

import java.io.File;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.Toast;

/**
 * 获取图像对话框
 * 
 * @author LynnChurch
 * @version 创建时间:2015年7月20日 下午3:28:00
 * 
 */
public class GetImageDialog extends Dialog
{
	public static final int REQUEST_CAREMA = 1; // 拍照
	public static final int REQUEST_GALLERY = 2; // 从相册中选择
	public static final int REQUEST_CROP = 3; // 剪裁

	private int mLayoutResID;
	private Activity mActivity;
	private int mAspectX = 1; // 剪裁框宽的权重
	private int mAspectY = 1; // 剪裁框高的权重
	private int mOutputX = 144; // 输出图像的宽
	private int mOutputY = 144; // 输出图像的高
	private File mTempFile; // 存储图像的临时路径
	private String mImageName = "image.jpg"; // 图像名称
	private boolean mIsCircleImage; // 是否为圆形图像

	public GetImageDialog(Activity activity)
	{
		this(activity, 0, 0);
		// TODO Auto-generated constructor stub
	}

	public GetImageDialog(Activity activity, int layoutResID)
	{
		this(activity, 0, layoutResID);
		// TODO Auto-generated constructor stub
	}

	public GetImageDialog(Activity activity, int theme, int layoutResID)
	{
		super(activity, R.style.DialogStyle);
		mActivity = activity;
		mLayoutResID = layoutResID;
		init();
		// TODO Auto-generated constructor stub
	}

	private void init()
	{
		if (0 == mLayoutResID)
		{
			mLayoutResID = R.layout.layout_getimage_dialog;
		}
		LayoutInflater inflater = LayoutInflater.from(mActivity);
		View v = inflater.inflate(mLayoutResID, null);
		Button camera = (Button) v.findViewById(R.id.btn_camera);
		Button gallery = (Button) v.findViewById(R.id.btn_gallery);
		Button cancel = (Button) v.findViewById(R.id.btn_cancel);
		camera.setOnClickListener(mListener);
		gallery.setOnClickListener(mListener);
		cancel.setOnClickListener(mListener);
		// 设置对话框的视图
		LayoutParams params = new LayoutParams(550, LayoutParams.WRAP_CONTENT);
		setContentView(v, params);
	}

	private View.OnClickListener mListener = new View.OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			// TODO Auto-generated method stub
			dismiss();
			int id = v.getId();
			// 判断存储卡是否可以用，可用进行存储
			if (hasSdcard())
			{
				mTempFile = new File(Environment.getExternalStorageDirectory()
						+ "/image", mImageName);
			} else
			{
				Toast.makeText(mActivity, "错误：未检测到SD卡", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			if (id == R.id.btn_camera)
			{
				Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
				// 从文件中创建uri
				Uri uri = Uri.fromFile(mTempFile);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
				mActivity.startActivityForResult(intent, REQUEST_CAREMA);

			} else if (id == R.id.btn_gallery)
			{
				Intent intent = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				mActivity.startActivityForResult(intent, REQUEST_GALLERY);
			} else if (id == R.id.btn_cancel)
			{
			} else
			{
			}
		}
	};

	/**
	 * 获取Bitmap,在onActivityResult()方法中使用
	 * 
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 * @return
	 */
	public Bitmap getBitmap(int requestCode, int resultCode, Intent data)
	{
		Bitmap bitmap = null;
		Rect rect = null;
		if (Activity.RESULT_OK != resultCode)
		{
			return null;
		}
		if (requestCode == REQUEST_GALLERY)
		{
			// 从相册返回的数据
			if (data != null)
			{
				crop(data.getData());
			}
		}
		if (requestCode == REQUEST_CAREMA)
		{
			// 从相机返回的数据
			if (hasSdcard())
			{
				crop(Uri.fromFile(mTempFile));
			} else
			{
				Toast.makeText(mActivity, "未找到存储卡，无法存储照片！", Toast.LENGTH_SHORT)
						.show();
			}
		}
		if (requestCode == REQUEST_CROP)
		{
			// 从剪切图片返回的数据
			try
			{
				bitmap = BitmapFactory.decodeStream(mActivity
						.getContentResolver().openInputStream(
								Uri.fromFile(mTempFile)));
				rect = data.getParcelableExtra("cropped-rect");
			} catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (null != mTempFile)
			{
				mTempFile.delete();
				mTempFile = null;
			}
		}
		if (null != bitmap)
		{
			if (mIsCircleImage)
			{
				bitmap = createCircleImage(bitmap, rect);
			}
		}
		return bitmap;
	}

	/*
	 * 剪切图片
	 */
	private void crop(Uri uri)
	{
		// 裁剪图片意图
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		// 裁剪框的比例
		intent.putExtra("aspectX", mAspectX);
		intent.putExtra("aspectY", mAspectY);
		// 裁剪后输出图片的尺寸大小
		intent.putExtra("outputX", mOutputX);
		intent.putExtra("outputY", mOutputY);
		intent.putExtra("scale", true);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());// 图片格式
		intent.putExtra("noFaceDetection", true);// 取消人脸识别
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTempFile));
		intent.putExtra("return-data", true);
		mActivity.startActivityForResult(intent, REQUEST_CROP);
	}

	/**
	 * 设置图像剪裁比例
	 * 
	 * @param cropW
	 *            宽所占比
	 * @param cropH
	 *            高所占比
	 */
	public void setCropRatio(int cropW, int cropH)
	{
		mAspectX = cropW;
		mAspectY = cropH;
	}

	/**
	 * 设置输出图像的尺寸
	 * 
	 * @param width
	 * @param height
	 */
	public void setOutputImageSize(int width, int height)
	{
		mOutputX = width;
		mOutputY = height;
	}

	/**
	 * 设置是否为圆形图像
	 * 
	 * @param isCircleImage
	 */
	public void setCircleImage(boolean isCircleImage)
	{
		mIsCircleImage = isCircleImage;
	}

	/**
	 * 判断sdcard是否被挂载
	 */
	private boolean hasSdcard()
	{
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED))
		{
			return true;
		} else
		{
			return false;
		}
	}

	/**
	 * 绘制圆形图片
	 * 
	 * @param source
	 *            图像所在的位图的对象
	 * @param rect
	 *            图像的尺寸
	 * @return
	 */
	private static Bitmap createCircleImage(Bitmap source, Rect rect)
	{
		int width = source.getWidth();
		int canvasW = width; // 画布宽
		int cropW = 0; // 裁剪宽
		int drawX = 0; // 位图绘制x坐标
		int drawY = 0; // 位图绘制y坐标
		if (null != rect)
		{
			cropW = rect.width() - 1;
			/*
			 * 当对小图缩小剪裁的时候，bitmap中实际图像的尺寸小于bitmap的尺寸，
			 * 为避免进行圆形剪裁出现问题，所以得对canvas的大小进行调整
			 */
			if (cropW < 160)
			{
				canvasW = cropW;
				// 调整绘制坐标，使bitmap中的图像恰好绘制在canvas中
				drawX = (cropW - width) / 2;
				drawY = drawX;
			}
		}
		final Paint paint = new Paint();
		// 抗锯齿
		paint.setAntiAlias(true);
		Bitmap target = Bitmap.createBitmap(canvasW, canvasW, Config.ARGB_8888);
		/**
		 * 产生一个同样大小的画布
		 */
		Canvas canvas = new Canvas(target);
		/**
		 * 首先绘制圆形
		 */
		canvas.drawCircle(canvasW / 2, canvasW / 2, canvasW / 2, paint);
		/**
		 * 使用SRC_IN
		 */
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		/**
		 * 绘制图片
		 */
		canvas.drawBitmap(source, drawX, drawY, paint);
		return target;
	}
}
