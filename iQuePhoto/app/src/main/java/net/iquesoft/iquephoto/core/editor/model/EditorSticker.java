package net.iquesoft.iquephoto.core.editor.model;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;

import net.iquesoft.iquephoto.util.RectUtil;

// TODO: Make abstract class for this and EditorText.
public class EditorSticker {
    private static final float MIN_SCALE = 0.15f;
    private static final int HELP_BOX_PAD = 25;
    private static final int BUTTON_WIDTH = 30;

    private float mX;
    private float mY;

    public Bitmap mBitmap;

    private RectF mImageRect;

    private Rect mSrcRect;
    private RectF mDstRect;

    private RectF mFrameRect;

    private Rect mRotateHandleSrcRect;
    private Rect mResizeHandleSrcRect;
    private Rect mDeleteHandleSrcRect;
    private Rect mFrontHandleSrcRect;

    private RectF mDeleteHandleDstRect;
    private RectF mRotateHandleDstRect;
    private RectF mResizeHandleDstRect;
    private RectF mFrontHandleDstRect;

    private Matrix mMatrix;
    private float mRotateAngle = 0;
    boolean isDrawHelpTool = false;

    private float initWidth;

    private EditorFrame mEditorFrame;

    public EditorSticker(Bitmap bitmap, RectF imageRect, EditorFrame editorFrame) {
        mBitmap = bitmap;

        mImageRect = imageRect;
        mEditorFrame = editorFrame;

        initialize();
    }

    public void initialize() {
        mSrcRect = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());

        int stickerWidth = Math.min(mBitmap.getWidth(), (int) mImageRect.width() >> 1);
        int stickerHeight = stickerWidth * mBitmap.getHeight() / mBitmap.getWidth();

        float left = mImageRect.centerX() - (stickerWidth / 2);
        float top = mImageRect.centerY() - (stickerHeight / 2);

        mDstRect = new RectF(left, top, left + stickerWidth, top + stickerHeight);

        mMatrix = new Matrix();
        mMatrix.postTranslate(mDstRect.left, mDstRect.top);
        mMatrix.postScale((float) stickerWidth / mBitmap.getWidth(),
                (float) stickerHeight / mBitmap.getHeight(), mDstRect.left,
                mDstRect.top);

        initWidth = mDstRect.width();

        isDrawHelpTool = true;

        mFrameRect = new RectF(mDstRect);
        updateFrameRect();

        mRotateHandleSrcRect = new Rect(0, 0, mEditorFrame.getDeleteHandleBitmap().getWidth(),
                mEditorFrame.getDeleteHandleBitmap().getHeight());
        mDeleteHandleSrcRect = new Rect(0, 0, mEditorFrame.getResizeHandleBitmap().getWidth(),
                mEditorFrame.getResizeHandleBitmap().getHeight());
        mResizeHandleSrcRect = new Rect(0, 0, mEditorFrame.getRotateHandleBitmap().getWidth(),
                mEditorFrame.getRotateHandleBitmap().getHeight());
        mFrontHandleSrcRect = new Rect(0, 0, mEditorFrame.getFrontHandleBitmap().getWidth(),
                mEditorFrame.getFrontHandleBitmap().getHeight());

        int handleHalfSize = mEditorFrame.getDeleteHandleBitmap().getWidth() / 2;

        mDeleteHandleDstRect = new RectF(0, 0, handleHalfSize << 1, handleHalfSize << 1);
        mResizeHandleDstRect = new RectF(0, 0, handleHalfSize << 1, handleHalfSize << 1);
        mFrontHandleDstRect = new RectF(0, 0, handleHalfSize << 1, handleHalfSize << 1);
        mRotateHandleDstRect = new RectF(0, 0, handleHalfSize << 1, handleHalfSize << 1);
        /*mFrameHandlesRect = new Rect(0, 0, mEditorFrame.getDeleteHandleBitmap().getWidth(),
                mEditorFrame.getDeleteHandleBitmap().getHeight());

        mDeleteHandleRect = new RectF(mFrameRect.left - BUTTON_WIDTH, mFrameRect.top
                - BUTTON_WIDTH, mFrameRect.left + BUTTON_WIDTH, mFrameRect.top
                + BUTTON_WIDTH);
        mResizeHandleRect = new RectF(mFrameRect.right - BUTTON_WIDTH, mFrameRect.bottom
                - BUTTON_WIDTH, mFrameRect.right + BUTTON_WIDTH, mFrameRect.bottom
                + BUTTON_WIDTH);

        mResizeHandleDstRect = new RectF(mResizeHandleRect);
        mDeleteHandleDstRect = new RectF(mDeleteHandleRect);*/
    }

    private void updateFrameRect() {
        mFrameRect.left -= HELP_BOX_PAD;
        mFrameRect.right += HELP_BOX_PAD;
        mFrameRect.top -= HELP_BOX_PAD;
        mFrameRect.bottom += HELP_BOX_PAD;
    }

    public void actionMove(float dx, float dy) {
        mMatrix.postTranslate(dx, dy);

        mDstRect.offset(dx, dy);

        mFrameRect.offset(dx, dy);
        //mDeleteHandleRect.offset(dx, dy);
        //mResizeHandleRect.offset(dx, dy);

        //mResizeHandleDstRect.offset(dx, dy);
        //mDeleteHandleDstRect.offset(dx, dy);
    }

    public void updateRotateAndScale(final float oldx, final float oldy,
                                     final float dx, final float dy) {
        float c_x = mDstRect.centerX();
        float c_y = mDstRect.centerY();

        float x = mResizeHandleDstRect.centerX();
        float y = mResizeHandleDstRect.centerY();

        float n_x = x + dx;
        float n_y = y + dy;

        float xa = x - c_x;
        float ya = y - c_y;

        float xb = n_x - c_x;
        float yb = n_y - c_y;

        float srcLen = (float) Math.sqrt(xa * xa + ya * ya);
        float curLen = (float) Math.sqrt(xb * xb + yb * yb);

        float scale = curLen / srcLen;

        float newWidth = mDstRect.width() * scale;
        if (newWidth / initWidth < MIN_SCALE) {
            return;
        }

        mMatrix.postScale(scale, scale, mDstRect.centerX(),
                mDstRect.centerY());

        RectUtil.scaleRect(mDstRect, scale);

        mFrameRect.set(mDstRect);
        updateFrameRect();
        /*mResizeHandleRect.offsetTo(mFrameRect.right - BUTTON_WIDTH, mFrameRect.bottom
                - BUTTON_WIDTH);
        mDeleteHandleRect.offsetTo(mFrameRect.left - BUTTON_WIDTH, mFrameRect.top
                - BUTTON_WIDTH);*/

        mResizeHandleDstRect.offsetTo(mFrameRect.right - BUTTON_WIDTH, mFrameRect.bottom
                - BUTTON_WIDTH);
        mDeleteHandleDstRect.offsetTo(mFrameRect.left - BUTTON_WIDTH, mFrameRect.top
                - BUTTON_WIDTH);

        double cos = (xa * xb + ya * yb) / (srcLen * curLen);
        if (cos > 1 || cos < -1)
            return;
        float angle = (float) Math.toDegrees(Math.acos(cos));

        float calMatrix = xa * yb - xb * ya;

        int flag = calMatrix > 0 ? 1 : -1;
        angle = flag * angle;

        mRotateAngle += angle;
        mMatrix.postRotate(angle, mDstRect.centerX(),
                mDstRect.centerY());

        RectUtil.rotateRect(mResizeHandleDstRect, mDstRect.centerX(),
                mDstRect.centerY(), mRotateAngle);
        RectUtil.rotateRect(mDeleteHandleDstRect, mDstRect.centerX(),
                mDstRect.centerY(), mRotateAngle);

    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, mMatrix, null);

        canvas.save();
        canvas.rotate(mRotateAngle, mFrameRect.centerX(), mFrameRect.centerY());
        canvas.drawRect(mFrameRect, mEditorFrame.getPaint());
        canvas.restore();

        int offsetValue = ((int) mDeleteHandleDstRect.width()) >> 1;

        mDeleteHandleDstRect.offsetTo(mFrameRect.left - offsetValue, mFrameRect.top - offsetValue);
        mResizeHandleDstRect.offsetTo(mFrameRect.right - offsetValue, mFrameRect.bottom - offsetValue);
        mRotateHandleDstRect.offsetTo(mFrameRect.right - offsetValue, mFrameRect.top - offsetValue);
        mFrontHandleDstRect.offsetTo(mFrameRect.left - offsetValue, mFrameRect.bottom - offsetValue);

        RectUtil.rotateRect(mDeleteHandleDstRect, mFrameRect.centerX(),
                mFrameRect.centerY(), mRotateAngle);

        RectUtil.rotateRect(mRotateHandleDstRect, mFrameRect.centerX(),
                mFrameRect.centerY(), mRotateAngle);

        RectUtil.rotateRect(mResizeHandleDstRect, mFrameRect.centerX(),
                mFrameRect.centerY(), mRotateAngle);

        RectUtil.rotateRect(mFrontHandleDstRect, mFrameRect.centerX(),
                mFrameRect.centerY(), mRotateAngle);

        canvas.drawBitmap(mEditorFrame.getDeleteHandleBitmap(),
                mDeleteHandleSrcRect,
                mDeleteHandleDstRect,
                null);

        canvas.drawBitmap(mEditorFrame.getRotateHandleBitmap(),
                mRotateHandleSrcRect,
                mRotateHandleDstRect,
                null);

        canvas.drawBitmap(mEditorFrame.getResizeHandleBitmap(),
                mResizeHandleSrcRect,
                mResizeHandleDstRect,
                null);

        canvas.drawBitmap(mEditorFrame.getFrontHandleBitmap(),
                mFrontHandleSrcRect,
                mFrontHandleDstRect,
                null);


    }

    private void drawFrame(Canvas canvas) {

    }

    public boolean isInside(MotionEvent event) {
        return mDstRect.contains(event.getX(), event.getY());
    }

    public boolean isInDeleteHandleButton(MotionEvent event) {
        return mDeleteHandleDstRect.contains(event.getX(), event.getY());
    }

    public boolean isInResizeHandleButton(MotionEvent event) {
        return mResizeHandleDstRect.contains(event.getX(), event.getY());
    }

    public boolean isInFrontHandleButton(MotionEvent event) {
        return mFrontHandleDstRect.contains(event.getX(), event.getY());
    }
}
