package com.lynnchurch.getimagedialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.lynnchurch.cropimage.CropImageActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;
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
	public final String TEMP_PHOTO_FILE_NAME = "image.png"; // 图像名称

	private int mLayoutResID;
	private Activity mActivity;
	private int mAspectX = 1; // 剪裁框宽的权重
	private int mAspectY = 1; // 剪裁框高的权重
	private int mOutputX; // 输出图像的宽
	private int mOutputY; // 输出图像的高
	private File mTempFile; // 存储图像的临时路径
	private boolean mIsCircleImage; // 是否为圆形图像
	private boolean mNeedCrop = true; // 是否需要剪裁

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
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		setContentView(v, params);
	}

	private View.OnClickListener mListener = new View.OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			// TODO Auto-generated method stub
			dismiss();
			// 创建照片存储路径
			if (null == mTempFile)
			{
				if (hasSdcard())
				{
					mTempFile = new File(Environment
							.getExternalStorageDirectory().getAbsolutePath(),
							TEMP_PHOTO_FILE_NAME);
				} else
				{
					mTempFile = new File(mActivity.getFilesDir()
							.getAbsolutePath(), TEMP_PHOTO_FILE_NAME);
				}
			}
			int id = v.getId();
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
		if (Activity.RESULT_OK != resultCode)
		{
			return null;
		}
		if (requestCode == REQUEST_GALLERY)
		{
			// 从相册返回的数据
			if (data != null)
			{
				try
				{
					InputStream inputStream = mActivity.getContentResolver()
							.openInputStream(data.getData());
					FileOutputStream fileOutputStream = new FileOutputStream(
							mTempFile);
					copyStream(inputStream, fileOutputStream);
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (mNeedCrop)
				{
					crop();
				} else
				{
					bitmap = pathToBitmap();
				}
			}
		}
		if (requestCode == REQUEST_CAREMA)
		{
			// 从相机返回的数据
			if (mNeedCrop)
			{
				crop();
			} else
			{
				bitmap = pathToBitmap();
			}
		}
		if (requestCode == REQUEST_CROP)
		{
			// 从剪切图片返回的数据
			String path = data.getStringExtra(CropImageActivity.IMAGE_PATH);
			if (path == null)
			{

				return bitmap;
			}
			bitmap = pathToBitmap();
//			if (mIsCircleImage)
//			{
//				bitmap = createCircleImage(bitmap);
//			}
		}
		return bitmap;
	}

	private Bitmap pathToBitmap()
	{
		Bitmap bitmap = null;
		if (null != mTempFile)
		{
			bitmap = BitmapFactory.decodeFile(mTempFile.getPath());
			mTempFile.delete();
			mTempFile = null;
		}
		return bitmap;
	}

	/**
	 * 设置是否需要剪裁
	 * 
	 * @param need
	 */
	public void setNeedCrop(boolean need)
	{
		mNeedCrop = need;
	}

	/**
	 * 将相册的图片读出来
	 * 
	 * @param input
	 * @param output
	 * @throws IOException
	 */
	public static void copyStream(InputStream input, OutputStream output)
			throws IOException
	{

		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = input.read(buffer)) != -1)
		{
			output.write(buffer, 0, bytesRead);
		}
		input.close();
		output.close();
	}

	/**
	 * 剪裁图片
	 */
	private void crop()
	{

		Intent intent = new Intent(mActivity, CropImageActivity.class);
		intent.putExtra(CropImageActivity.IMAGE_PATH, mTempFile.getPath());
		intent.putExtra(CropImageActivity.SCALE, true);
		intent.putExtra(CropImageActivity.SCALE_UP_IF_NEEDED, true);

		// 裁剪框的比例
		intent.putExtra(CropImageActivity.ASPECT_X, mAspectX);
		intent.putExtra(CropImageActivity.ASPECT_Y, mAspectY);

		// 裁剪后输出图片的尺寸大小
		intent.putExtra(CropImageActivity.OUTPUT_X, mOutputX);
		intent.putExtra(CropImageActivity.OUTPUT_Y, mOutputY);

		intent.putExtra(CropImageActivity.CIRCLE_CROP, mIsCircleImage);
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
	 * @return
	 */
	private static Bitmap createCircleImage(Bitmap source)
	{
		int width = source.getWidth();
		int canvasW = width; // 画布宽
		int drawX = 0; // 位图绘制x坐标
		int drawY = 0; // 位图绘制y坐标
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
