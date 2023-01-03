package ddwu.moblie.finalproject.ma01_20200962;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ShowClothesAdapter extends CursorAdapter {

    LayoutInflater inflater;
    int layout;

    public ShowClothesAdapter(Context context, int layout, Cursor c) {
        super(context, c, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layout = layout;
    }

    @SuppressLint("Range")
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        if (holder.tvTitle == null) {
            holder.tvTitle = view.findViewById(R.id.tvTitle);
            holder.tvCategory = view.findViewById(R.id.tvCategory);
            holder.ivImage = view.findViewById(R.id.ivImage);
        }

        holder.tvTitle.setText(cursor.getString(cursor.getColumnIndex(ClothesDBHelper.COL_NAME)));
        holder.tvCategory.setText(cursor.getString(cursor.getColumnIndex(ClothesDBHelper.COL_CATEGORY)));

        String mCurrentPath = cursor.getString(cursor.getColumnIndex(ClothesDBHelper.COL_PHOTO));
        if(!mCurrentPath.equals("")){
            int targetW = holder.ivImage.getWidth();
            int targetH = holder.ivImage.getHeight();
            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mCurrentPath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
//      bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPath, bmOptions);
            holder.ivImage.setImageBitmap(bitmap);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View listItemLayout = inflater.inflate(layout, viewGroup, false);

//        ViewHolder 사용을 위해 ViewHolder 를 생성한 View 의 Tag 에 추가
        ViewHolder holder = new ViewHolder();
        listItemLayout.setTag(holder);

        return listItemLayout;
    }


    static class ViewHolder {
        TextView tvTitle;
        TextView tvCategory;
        ImageView ivImage;

        public ViewHolder() {
            tvTitle = null;
            tvCategory = null;
            ivImage = null;
        }
    }

}
