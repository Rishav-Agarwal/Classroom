package in.edu.jaduniv.classroom.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import in.edu.jaduniv.classroom.R;
import in.edu.jaduniv.classroom.object.Post;
import in.edu.jaduniv.classroom.helper.DownloadDbHelper;
import in.edu.jaduniv.classroom.utility.PermissionUtils;

public class PostAdapter extends ArrayAdapter<Post> {

    private Context context;
    private ArrayList<Post> postArrayList;
    private String classCode;

    public PostAdapter(@NonNull Context context, int resource, ArrayList<Post> posts, String classCode) {
        super(context, resource);
        this.context = context;
        postArrayList = posts;
        this.classCode = classCode;
    }

    @Override
    public void add(Post post) {
        if (post != null) {
            super.add(post);
            postArrayList.add(post);
        }
    }

    @Override
    public int getCount() {
        return postArrayList.size();
    }

    @Nullable
    @Override
    public Post getItem(int position) {
        return postArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.post, parent, false);
        }

        final View ConvertView = convertView;

        final Post post = postArrayList.get(position);

        final TextView tvSender = ConvertView.findViewById(R.id.tv_sender_name);
        final TextView tvPostTitle = ConvertView.findViewById(R.id.tv_post_title);
        final TextView tvPostDescription = ConvertView.findViewById(R.id.tv_post_description);
        final TextView tvPostTime = ConvertView.findViewById(R.id.tv_post_time);
        final LinearLayout llFile = ConvertView.findViewById(R.id.ll_file);

        String postedByName = post.getPostedByName();
        tvSender.setText(postedByName);
        tvPostTitle.setText(post.getTitle());
        if (post.getTitle() == null || post.getTitle().equals(""))
            tvPostTitle.setVisibility(View.GONE);
        else
            tvPostTitle.setVisibility(View.VISIBLE);
        tvPostDescription.setText(post.getDescription());

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        Date date = new Date(post.getLongTime());
        String strDate = dateFormat.format(date);

        SimpleDateFormat timeFormat = new SimpleDateFormat("KK:mm a");
        Date time = new Date(post.getLongTime());
        String strTime = timeFormat.format(time);

        String currDate = dateFormat.format(Calendar.getInstance().getTime());
        String currTime = timeFormat.format(Calendar.getInstance().getTime());

        if (strDate.equals(currDate)) {
            tvPostTime.setText(strTime);
        } else {
            Calendar yesterday = Calendar.getInstance(); // today
            yesterday.add(Calendar.DAY_OF_YEAR, -1); // yesterday

            Calendar postDate = Calendar.getInstance();
            postDate.setTime(date); // your date

            if (yesterday.get(Calendar.YEAR) == postDate.get(Calendar.YEAR)
                    && yesterday.get(Calendar.DAY_OF_YEAR) == postDate.get(Calendar.DAY_OF_YEAR)) {
                tvPostTime.setText(String.format("Yesterday,  %s", strTime));
            } else {
                tvPostTime.setText(String.format("%s,  %s", strDate, strTime));
            }
        }

        if (post.getUrl() != null && !post.getUrl().equals("null") && !post.getUrl().equals("") && !post.getUrl().equals("undefined")) {
            llFile.setVisibility(View.VISIBLE);
            TextView tvFileName = llFile.findViewById(R.id.tv_file_name);
            tvFileName.setText(post.getFileName());

            llFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

                    //Get download reference for the syllabus if available
                    DownloadDbHelper helper = DownloadDbHelper.getInstance(context);
                    Long downloadReference = helper.getDownloadId(context.getPackageName() + "." + classCode + ".Posts." + post.getLongTime());
                    Log.d("downloadReference", downloadReference + "");

                    long downloadId = 0;

                    if (dm != null && downloadReference != null) {
                        //Perform actions depending on the status of the download
                        DownloadManager.Query query = new DownloadManager.Query();
                        query.setFilterById(downloadReference);

                        Cursor cursor = dm.query(query);
                        Log.d("cursor", cursor + "");
                        if (cursor == null || !cursor.moveToFirst()) {
                            Log.d("downloadId", downloadId + "");
                            Long _id = downloadFile(position);
                            if (_id == null)
                                return;
                            downloadId = _id;
                            DownloadDbHelper.getInstance(context).addDownload(context.getPackageName() + "." + classCode + ".Posts." + post.getLongTime(), downloadId);
                            return;
                        }
                        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        int downloadStatus = cursor.getInt(columnIndex);
                        int filePathInt = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                        String filePath = cursor.getString(filePathInt);

                        Long _id;
                        switch (downloadStatus) {
                            case DownloadManager.STATUS_FAILED:
                                Log.d("Download status", "STATUS_FAILED");
                            case DownloadManager.ERROR_FILE_ERROR:
                                Log.d("Download status", "ERROR_FILE_ERROR");
                                boolean deleted = deleteDoc(filePath);
                                _id = downloadFile(position);
                                if (_id == null)
                                    break;
                                downloadId = _id;
                                DownloadDbHelper.getInstance(context).addDownload(context.getPackageName() + "." + classCode + ".Posts." + post.getLongTime(), downloadId);
                                break;

                            case DownloadManager.STATUS_SUCCESSFUL:
                                Log.d("Download status", "STATUS_SUCCESSFUL");
                                if (filePath != null) {
                                    if (!openDoc(filePath)) {
                                        boolean isDeleted = deleteDoc(filePath);
                                        _id = downloadFile(position);
                                        if (_id == null)
                                            break;
                                        downloadId = _id;
                                        DownloadDbHelper.getInstance(context).addDownload(context.getPackageName() + "." + classCode + ".Posts." + post.getLongTime(), downloadId);
                                    }
                                } else {
                                    downloadFile(position);
                                    DownloadDbHelper.getInstance(context).addDownload(context.getPackageName() + "." + classCode + ".Posts." + post.getLongTime(), downloadId);
                                }
                                break;

                            case DownloadManager.STATUS_PENDING:
                            case DownloadManager.STATUS_RUNNING:
                            case DownloadManager.STATUS_PAUSED:
                                Log.d("Download status", "STATUS_PENDING | RUNNING | PAUSED");
                                dm.remove(downloadReference);
                                boolean isDeleted = deleteDoc(filePath);
                                _id = downloadFile(position);
                                if (_id == null)
                                    break;
                                downloadId = _id;
                                DownloadDbHelper.getInstance(context).addDownload(context.getPackageName() + "." + classCode + ".Posts." + post.getLongTime(), downloadId);
                                break;
                        }
                    } else {
                        Log.d("downloadId", downloadId + "");
                        Long _id = downloadFile(position);
                        if (_id == null)
                            return;
                        downloadId = _id;
                        DownloadDbHelper.getInstance(context).addDownload(context.getPackageName() + "." + classCode + ".Posts." + post.getLongTime(), downloadId);
                    }

                }
            });

            llFile.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onPopupMenuItemCLicked(v, position);
                    return true;
                }
            });
        } else {
            llFile.setVisibility(View.GONE);
        }

        return ConvertView;
    }

    private Long downloadWithPermission(int position) {
        Long downloadId = null;
        Post post = getItem(position);
        if (post != null) {
            try {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(post.getUrl()));
                request.setDescription(classCode + "_Posts_" + post.getFileName());
                request.setTitle("Classroom");
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir("", "/Classroom/" + classCode + "/Posts/" + post.getFileName());
                DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                if (manager != null) {
                    downloadId = manager.enqueue(request);
                }
            } catch (Exception e) {
                Log.e("Error dowloading file", e.getMessage());
                e.printStackTrace();
            }
        }
        return downloadId;
    }

    private Long downloadFile(int position) {
        if (PermissionUtils.isPermitted(context, PermissionUtils.Permissions.WRITE_EXTERNAL_STORAGE))
            return downloadWithPermission(position);
        PermissionUtils.request((Activity) context, PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.Permissions.WRITE_EXTERNAL_STORAGE);
        return null;
    }

    private boolean deleteDoc(String uri) {
        boolean deleted = false;
        try {
            File file = new File(Uri.parse(uri).getPath());
            deleted = file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("File deleted?", deleted + "");
        return deleted;
    }

    private boolean openDoc(String uri) {
        Intent open = new Intent(Intent.ACTION_VIEW);
        open.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri);
        if (uri != null && extension != null) {
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            open.setDataAndType(FileProvider.getUriForFile(context, context.getPackageName() + ".provider", new File(Uri.parse(uri).getPath())), mimeType);
            Intent openChooser = Intent.createChooser(open, "Choose app");
            context.startActivity(openChooser);
            return true;
        }
        return false;
    }

    @SuppressLint("RestrictedApi")
    private void onPopupMenuItemCLicked(View anchor, final int position) {
        final Post post = getItem(position);
        MenuBuilder menuBuilder = new MenuBuilder(context);
        MenuInflater menuInflater = new MenuInflater(context);
        menuInflater.inflate(R.menu.menu_syllabus_item, menuBuilder);
        MenuPopupHelper downloadMenu = new MenuPopupHelper(context, menuBuilder, anchor);
        downloadMenu.setForceShowIcon(true);

        // Set Item Click Listener
        menuBuilder.setCallback(new MenuBuilder.Callback() {
            @Override
            public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                Long _id = downloadFile(position);
                if (_id == null)
                    return false;
                long downloadId = _id;
                if (post != null)
                    DownloadDbHelper.getInstance(context).addDownload(context.getPackageName() + "." + classCode + ".Posts." + post.getLongTime(), downloadId);
                return false;
            }

            @Override
            public void onMenuModeChange(MenuBuilder menu) {
            }
        });

        // Display the menu
        downloadMenu.show();
    }
}