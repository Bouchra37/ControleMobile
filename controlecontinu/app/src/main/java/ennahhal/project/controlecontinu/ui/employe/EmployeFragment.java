package ennahhal.project.controlecontinu.ui.employe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ennahhal.project.controlecontinu.R;


public class EmployeFragment extends Fragment {

    private EditText nomEmploye;
    private EditText prenomEmploye;
    private EditText dateEmploye;


    private Button bnAddEmploye;
    private String insertEmployeUrl = "http://10.0.2.2:8080/controle/employe";
    private String listEmployeUrl = "http://10.0.2.2:8080/controle/employe";
    private String listServiceUrl = "http://10.0.2.2:8080/controle/service";

    LinearLayout employeListLayout;
    Spinner spinnerService;

    private List<String> servicesList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_employe, container, false);

        nomEmploye = view.findViewById(R.id.nom);
        prenomEmploye = view.findViewById(R.id.prenom);
        dateEmploye= view.findViewById(R.id.date);
        bnAddEmploye = view.findViewById(R.id.bnAdd);
        employeListLayout = view.findViewById(R.id.employeListLayout);
        spinnerService = view.findViewById(R.id.serviceSpinner);

        bnAddEmploye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEmployeToDatabase();
            }
        });

        spinnerService.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });

        fetchDataAndPopulateEmployeList();
        fetchServices();

        return view;
    }


    private void addEmployeToDatabase() {
        String selectedService = spinnerService.getSelectedItem().toString();
        int serviceId = getServiceIdByName(selectedService);
        String nomText = nomEmploye.getText().toString();
        String prenomText = prenomEmploye.getText().toString();
        String dateText = dateEmploye.getText().toString();
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("id", 0);
            jsonBody.put("nom", nomText);
            jsonBody.put("prenom", prenomText);
            jsonBody.put("dateNaissance", dateText);

            JSONObject serviceObject = new JSONObject();
            serviceObject.put("id", serviceId);
            jsonBody.put("service", serviceObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, insertEmployeUrl, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        handleAddEmployeResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleErrorAddingEmploye(error);
            }
        });

        requestQueue.add(request);
    }
    private int getServiceIdByName(String serviceName) {
        for (int i = 0; i < servicesList.size(); i++) {
            if (servicesList.get(i).equals(serviceName)) {
                return i + 1;
            }
        }
        return -1;
    }
    private void handleAddEmployeResponse(JSONObject response) {
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setMessage("Ajout d'employe avec succès")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                fetchDataAndPopulateEmployeList();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private void handleErrorAddingEmploye(VolleyError error) {
        Log.e("Fragment", "Error adding employe: " + error.getMessage());
    }

    private void fetchDataAndPopulateEmployeList() {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, listEmployeUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        employeListLayout.removeAllViews();

                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject employeObject = response.getJSONObject(i);
                                String employeId = employeObject.getString("id");
                                String employeNom= employeObject.getString("nom");
                                String employePrenom= employeObject.getString("prenom");
                                String employeDate= employeObject.getString("dateNaissance");

                                String service = employeObject.getJSONObject("service").getString("nom");

                                View listItemView = LayoutInflater.from(requireContext()).inflate(R.layout.list_item_employe, null);

                                TextView nomTextView = listItemView.findViewById(R.id.NomTextView);
                                TextView prenomTextView = listItemView.findViewById(R.id.PrenomTextView);
                                TextView dateTextView = listItemView.findViewById(R.id.DateTextView);


                                TextView serviceTextView = listItemView.findViewById(R.id.ServiceTextView);


                                serviceTextView.setText(service);
                                nomTextView.setText(employeNom);
                                prenomTextView.setText(employePrenom);
                                dateTextView.setText(employeDate);




                                listItemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        showOptionsEmployeDialog(employeId);
                                    }
                                });

                                employeListLayout.addView(listItemView);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Fragment", "Error fetching employe data: " + error.getMessage());
            }
        });

        requestQueue.add(request);
    }

    private void showOptionsEmployeDialog(final String employeId) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
        dialogBuilder.setTitle("Options");

        dialogBuilder.setPositiveButton("Modifier", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showEditEmployeDialog(employeId);
            }
        });

        dialogBuilder.setNegativeButton("Supprimer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showConfirmationEmployeDialog(employeId);
            }
        });

        dialogBuilder.show();
    }

    private void showEditEmployeDialog(final String employeId) {

        AlertDialog.Builder editDialogBuilder = new AlertDialog.Builder(requireContext());
        editDialogBuilder.setTitle("");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.edit_layout_employe, null);
        editDialogBuilder.setView(dialogView);

        // Initialisez les vues pour l'édition des données de l'employe

        editDialogBuilder.setPositiveButton("Enregistrer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fetchDataAndPopulateEmployeList();
            }
        });

        editDialogBuilder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog editDialog = editDialogBuilder.create();
        editDialog.show();
    }

    private void showConfirmationEmployeDialog(final String employeId) {
        AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(requireContext());
        confirmDialogBuilder.setMessage("Voulez-vous vraiment supprimer cet employe ?");
        confirmDialogBuilder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteEmploye(employeId);
            }
        });

        confirmDialogBuilder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        confirmDialogBuilder.show();
    }

    private void deleteEmploye(final String employeId) {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        String deleteUrl = "http://10.0.2.2:8080/controle/service/" + employeId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, deleteUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(requireContext(), "Employe supprimé avec succès", Toast.LENGTH_SHORT).show();
                        fetchDataAndPopulateEmployeList();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Fragment", "Error deleting employe: " + error.getMessage());
            }
        });

        requestQueue.add(request);
    }

    private void fetchServices() {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, listServiceUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        servicesList.clear();

                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject serviceObject = response.getJSONObject(i);
                                String service = serviceObject.getString("nom");
                                servicesList.add(service);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, servicesList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerService.setAdapter(adapter);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(requireContext(), "Erreur lors de la récupération des services", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(request);
    }
}
