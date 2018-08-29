package in.edu.jaduniv.classroom.adapters;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import in.edu.jaduniv.classroom.R;
import in.edu.jaduniv.classroom.object.Post;
import in.edu.jaduniv.classroom.other.DownloadDbHelper;
import in.edu.jaduniv.classroom.utility.CloudinaryUtils;
import in.edu.jaduniv.classroom.utility.FirebaseUtils;

public class PostRequestAdapter extends RecyclerView.Adapter<PostRequestAdapter.PostRequestViewHolder> {

    private ArrayList<Post> postRequests;
    private ArrayList<String> postRequestKeys;
    private Context context;
    private String classCode;
    private RecyclerViewEmptyListener emptyListener;

    public PostRequestAdapter(Context context, ArrayList<Post> postRequests, ArrayList<String> postRequestKeys, String classCode, RecyclerViewEmptyListener emptyListener) {
        this.emptyListener = emptyListener;
        this.context = context;
        this.postRequests = postRequests;
        this.postRequestKeys = postRequestKeys;
        this.classCode = classCode;
        if (postRequests.isEmpty()) {
            emptyListener.onRecyclerViewEmptied();
        } else {
            emptyListener.onRecyclerViewPopulated();
        }
    }

    static class PostRequestViewHolder extends RecyclerView.ViewHolder {

        TextView tvSenderName, tvTitle, tvDescription, tvTime;
        ImageView ivAccept, ivReject;
        LinearLayout llFile;

        PostRequestViewHolder(View itemView) {
            super(itemView);
            tvSenderName = (TextView) itemView.findViewById(R.id.tv_post_request_sender_name);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_post_request_title);
            tvDescription = (TextView) itemView.findViewById(R.id.tv_post_request_description);
            tvTime = (TextView) itemView.findViewById(R.id.tv_post_request_time);
            ivAccept = (ImageView) itemView.findViewById(R.id.iv_post_request_accept);
            ivReject = (ImageView) itemView.findViewById(R.id.iv_post_request_reject);
            llFile = (LinearLayout) itemView.findViewById(R.id.ll_file);
        }
    }

    @Override
    public PostRequestAdapter.PostRequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_request, parent, false);
        return new PostRequestViewHolder(view);
    }

    public Post getItem(int position) {
        return postRequests.get(position);
    }

    @Override
    public int getItemCount() {
        return postRequests.size();
    }

    @Override
    public void onBindViewHolder(final PostRequestViewHolder holder, int position) {
        final Post request = postRequests.get(holder.getAdapterPosition());
        holder.tvSenderName.setText(request.getPostedByName());
        holder.tvTitle.setText(request.getTitle());
        holder.tvDescription.setText(request.getDescription());

        if (request.getTitle() == null || request.getTitle().trim().equals("")) {
            holder.tvTitle.setVisibility(View.GONE);
        } else {
            holder.tvTitle.setVisibility(View.VISIBLE);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        Date date = new Date(request.getLongTime());
        String strDate = dateFormat.format(date);

        SimpleDateFormat timeFormat = new SimpleDateFormat("KK:mm a", Locale.getDefault());
        Date time = new Date(request.getLongTime());
        String strTime = timeFormat.format(time);

        final String currDate = dateFormat.format(Calendar.getInstance().getTime());
        String currTime = timeFormat.format(Calendar.getInstance().getTime());

        if (strDate.equals(currDate)) {
            holder.tvTime.setText(strTime);
        } else {
            Calendar yesterday = Calendar.getInstance(); // today
            yesterday.add(Calendar.DAY_OF_YEAR, -1); // yesterday

            Calendar postDate = Calendar.getInstance();
            postDate.setTime(date); // your date

            if (yesterday.get(Calendar.YEAR) == postDate.get(Calendar.YEAR)
                    && yesterday.get(Calendar.DAY_OF_YEAR) == postDate.get(Calendar.DAY_OF_YEAR)) {
                holder.tvTime.setText(String.format("Yesterday,  %s", strTime));
            } else {
                holder.tvTime.setText(String.format("%s,  %s", strDate, strTime));
            }
        }

        holder.ivReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUtils.getDatabaseReference().child("classes").child(classCode).child("post_req").child(postRequestKeys.get(holder.getAdapterPosition())).removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        Toast.makeText(context, "Post request deleted!", Toast.LENGTH_SHORT).show();
                        int index = holder.getAdapterPosition();
                        postRequests.remove(index);
                        postRequestKeys.remove(index);
                        if (postRequests.isEmpty()) {
                            emptyListener.onRecyclerViewEmptied();
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Map<String, String> config = CloudinaryUtils.getCloudinaryConfig();
                                    config.remove("resource_type");
                                    config.put("resource_type", request.getResourceType());
                                    Log.d("Resource type", config.get("resource_type"));
                                    Map destroy = CloudinaryUtils.getInstance().uploader().destroy(request.getPublicId(), config);
                                    Log.d("Destroy", destroy + "");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                        notifyItemRemoved(index);
                    }
                });
            }
        });

        holder.ivAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUtils.getDatabaseReference().child("classes").child(classCode).child("post_req").child(postRequestKeys.get(holder.getAdapterPosition())).removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        Post postRequest = postRequests.get(holder.getAdapterPosition());
                        postRequest.setTime(ServerValue.TIMESTAMP);
                        int index = holder.getAdapterPosition();
                        postRequests.remove(index);
                        postRequestKeys.remove(index);
                        notifyItemRemoved(index);
                        if (postRequests.isEmpty()) {
                            emptyListener.onRecyclerViewEmptied();
                        }
                        FirebaseUtils.getDatabaseReference().child("classes").child(classCode).child("posts").push().setValue(postRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(context, "Successfully posted", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });

        if (request.getUrl() != null && !request.getUrl().equals("null") && !request.getUrl().equals("") && !request.getUrl().equals("undefined")) {
            holder.llFile.setVisibility(View.VISIBLE);
            TextView tvFileName = holder.llFile.findViewById(R.id.tv_file_name);
            tvFileName.setText(request.getFileName());

            holder.llFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

                    //Get download reference for the syllabus if available
                    DownloadDbHelper helper = DownloadDbHelper.getInstance(context);
                    Long downloadReference = helper.getDownloadId(context.getPackageName() + "." + classCode + ".PostRequests." + request.getLongTime());
                    Log.d("downloadReference", downloadReference + "");

                    long downloadId = 0;

                    if (dm != null && downloadReference != null) {
                        //Perform actions depending on the status of the download
                        DownloadManager.Query query = new DownloadManager.Query();
                        query.setFilterById(downloadReference);

                        Cursor cursor = dm.query(query);
                        Log.d("cursor", cursor + "");
                        if (cursor == null || !cursor.moveToFirst()) {
                            downloadId = downloadFile(holder.getAdapterPosition());
                            Log.d("downloadId", downloadId + "");
                            DownloadDbHelper.getInstance(context).addDownload(context.getPackageName() + "." + classCode + ".Post." + request.getLongTime(), downloadId);
                            return;
                        }
                        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        int downloadStatus = cursor.getInt(columnIndex);
                        int filePathInt = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                        String filePath = cursor.getString(filePathInt);

                        switch (downloadStatus) {
                            case DownloadManager.STATUS_FAILED:
                                Log.d("Download status", "STATUS_FAILED");
                            case DownloadManager.ERROR_FILE_ERROR:
                                Log.d("Download status", "ERROR_FILE_ERROR");
                                boolean deleted = deleteDoc(filePath);
                                downloadId = downloadFile(holder.getAdapterPosition());
                                DownloadDbHelper.getInstance(context).addDownload(context.getPackageName() + "." + classCode + ".PostRequests." + request.getLongTime(), downloadId);
                                break;

                            case DownloadManager.STATUS_SUCCESSFUL:
                                Log.d("Download status", "STATUS_SUCCESSFUL");
                                if (filePath != null) {
                                    if (!openDoc(filePath)) {
                                        boolean isDeleted = deleteDoc(filePath);
                                        downloadId = downloadFile(holder.getAdapterPosition());
                                        DownloadDbHelper.getInstance(context).addDownload(context.getPackageName() + "." + classCode + ".PostRequests." + request.getLongTime(), downloadId);
                                    }
                                } else {
                                    downloadFile(holder.getAdapterPosition());
                                    DownloadDbHelper.getInstance(context).addDownload(context.getPackageName() + "." + classCode + ".PostRequests." + request.getLongTime(), downloadId);
                                }
                                break;

                            case DownloadManager.STATUS_PENDING:
                            case DownloadManager.STATUS_RUNNING:
                            case DownloadManager.STATUS_PAUSED:
                                Log.d("Download status", "STATUS_PENDING | RUNNING | PAUSED");
                                dm.remove(downloadReference);
                                boolean isDeleted = deleteDoc(filePath);
                                downloadId = downloadFile(holder.getAdapterPosition());
                                DownloadDbHelper.getInstance(context).addDownload(context.getPackageName() + "." + classCode + ".PostRequests." + request.getLongTime(), downloadId);
                                break;
                        }
                    } else {
                        downloadId = downloadFile(holder.getAdapterPosition());
                        Log.d("downloadId", downloadId + "");
                        DownloadDbHelper.getInstance(context).addDownload(context.getPackageName() + "." + classCode + ".PostRequests." + request.getLongTime(), downloadId);
                    }

                }
            });

            holder.llFile.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onPopupMenuItemCLicked(v, holder.getAdapterPosition());
                    return true;
                }
            });
        } else {
            holder.llFile.setVisibility(View.GONE);
        }
    }

    private Long downloadFile(int position) {
        Long downloadId = null;
        Post post = getItem(position);
        if (post != null) {
            try {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(post.getUrl()));
                request.setDescription(classCode + "_PostRequests_" + post.getFileName());
                request.setTitle("Classroom");
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir("", "/Classroom/" + classCode + "/PostRequests/" + post.getFileName());
                DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                if (manager != null) {
                    downloadId = manager.enqueue(request);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return downloadId;
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
        String mimeType;
        String extension;
        extension = MimeTypeMap.getFileExtensionFromUrl(uri);
        if (uri != null && extension != null) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
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
                long downloadId = downloadFile(position);
                if (post != null)
                    DownloadDbHelper.getInstance(context).addDownload(context.getPackageName() + "." + classCode + ".PostRequests." + post.getLongTime(), downloadId);
                return false;
            }

            @Override
            public void onMenuModeChange(MenuBuilder menu) {
            }
        });

        // Display the menu
        downloadMenu.show();
    }

    public interface RecyclerViewEmptyListener {
        void onRecyclerViewEmptied();

        void onRecyclerViewPopulated();
    }
}