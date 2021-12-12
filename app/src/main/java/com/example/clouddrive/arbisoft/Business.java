package com.example.clouddrive.arbisoft;

import java.util.Scanner;

public class Business {

    private Model[] businessModel;

    public Business(int totalInput) {
        businessModel = new Model[totalInput];
        // lets take inputs (=totalInputs)
        for (int numInputs = 0; numInputs < totalInput; numInputs++) {
            businessModel[numInputs] = getInput();
        }
        // At this stage we've got all the clients with their name and businessValue
    }

    private Model getInput() {
        Scanner scanner = new Scanner(System.in);
        String inputLine = scanner.nextLine();
        // trim the inputs to remove leading spaces
        Model inputModel = new Model(inputLine.split(",")[0].trim(), inputLine.split(",")[1].trim());
        return inputModel;
    }

    // Bubble Sort
    protected void prioritize() {
        int outer, inner;
        for (outer = 0; outer < businessModel.length; outer++) {
            for (inner = 0; inner < businessModel.length; inner++) {
                if (businessModel[outer].compare(businessModel[inner])) { // check if outer-loop model haven't got greater businessVal
                    Model tempModel = businessModel[outer];
                    businessModel[outer] = businessModel[inner];
                    businessModel[inner] = tempModel;
                }
            }
        }
        // at this stage we've sorted our based of businessValue
    }

    // Selection Sort
    protected void prioritize(boolean selectionSort) {
        int outer, inner;
        int min;
        for (outer = 0; outer < businessModel.length; outer++) {
            min = outer;
            for (inner = outer + 1; inner < businessModel.length; inner++) {
                if (businessModel[inner].compare(businessModel[min]))
                    min = inner;
            }
            Model tempModel = businessModel[outer];
            businessModel[outer] = businessModel[min];
            businessModel[min] = tempModel;
        }
        // at this stage we've sorted businessModel
    }

    // Insertion Sort
    protected void prioritize(int insertionSort) {
        int min;
        for (int inner = 1; inner < businessModel.length; inner++) {
            min = businessModel[inner].businessValue;

            for (int outer = inner - 1; outer >= 0; outer--) {
                if (businessModel[outer].compare(businessModel[inner])) {
                    Model tempModel = businessModel[outer];
                    businessModel[outer] = businessModel[min];
                    businessModel[min] = tempModel;
                }
            }

//            while (min < businessModel[inner - 1].businessValue) {
//
//            }

        }
    }
}
