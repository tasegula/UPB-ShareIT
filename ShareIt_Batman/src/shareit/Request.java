/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shareit;

import java.io.Serializable;

/**
 * 
 * @author andrei
 */
public abstract class Request implements Serializable {
    public boolean isResolved;
    public boolean isSuccess;
    public String message;

    public void solve(boolean isSuccess, String message) {
        this.isResolved = true;
        this.isSuccess = isSuccess;
        this.message = message;
    }
}
