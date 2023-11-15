package ennahhal.project.controlecontinu.ui.employe;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EmployeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public EmployeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is employe fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}