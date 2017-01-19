package ar.com.nicolasquartieri.ui.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ar.com.nicolasquartieri.R;
import ar.com.nicolasquartieri.widget.LoadingImageView;

/**
 * Display a full screen image.
 * @author Nicolas Quartieri (nicolas.quartieri@gmailn.com)
 */
public class ImageDialog extends DialogFragment {
    /** Image Argument */
    private static final String ARG_IMAGE = "ARG_IMAGE";
    /** URL String image */
    private String resImage;

    /** Default constructor. */
    public ImageDialog() {
    }

    /**
     * Factory method that returns a new instance of the image dialog fragment.
     *
     * @param imageUrl The url string to the image, can be null but a full transparent
     * screen will show up.
     * @return A new dialog fragment instance, never null.
     */
    public static ImageDialog getImageDialog(final String imageUrl) {
        ImageDialog imageDialog = new ImageDialog();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE, imageUrl);
        imageDialog.setArguments(args);
        return imageDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Full screen dialog.
        setStyle(STYLE_NO_TITLE, R.style.TranslucentDialog);
        // Get Arguments.
        Bundle args = getArguments();
        resImage = args.getString(ARG_IMAGE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_image_show, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Set's close button.
        View closeView = view.findViewById(R.id.close_window);
        closeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        // Load the required image.
        LoadingImageView image = (LoadingImageView) view.findViewById(R.id.image);
        image.setImageUrl(resImage);
    }
}
