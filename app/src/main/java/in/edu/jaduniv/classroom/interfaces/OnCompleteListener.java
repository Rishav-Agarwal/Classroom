package in.edu.jaduniv.classroom.interfaces;

import java.io.Serializable;

import in.edu.jaduniv.classroom.object.Post;

public interface OnCompleteListener extends Serializable {

    void onUploadCompleted(Post post);
}
