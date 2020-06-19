package shyunku.project.together.CustomViews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;

public class SquareProgressBarView extends ProgressBar {
    public SquareProgressBarView(Context context) {
        super(context);
    }

    public SquareProgressBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        setMeasuredDimension(width, width);
    }
}
