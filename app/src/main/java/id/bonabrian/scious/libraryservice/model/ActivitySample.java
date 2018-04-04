package id.bonabrian.scious.libraryservice.model;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public interface ActivitySample extends Timestamped {

    int NOT_MEASURED = -1;

    //TODO Get Sample
    //SampleProvider getProvider();

    int getRawKind();

    int getKind();

    int getRawIntensity();

    float getIntensity();

    int getSteps();

    int getHeartRate();

    void setHeartRate(int value);
}
