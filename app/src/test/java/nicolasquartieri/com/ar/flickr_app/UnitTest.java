package nicolasquartieri.com.ar.flickr_app;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class UnitTest {

    @Mock
    List otherList;

    @Test
    public void testListOneWay() {
        //Arrange
        List listMock = mock(List.class);

        //Act
        listMock.add("One");

        //Assert
        verify(listMock).add("One");
    }
}