package org.srpc.server.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * <pre>
 *     ServiceTask in server center
 * </pre>
 * @auth BillWu
 * @since 2018-01-01
 */
public class ServiceTask implements Runnable {
    private Socket client = null;

    public ServiceTask(Socket client) {
        this.client = client;
    }

    public void run() {
        ObjectInputStream input = null;
        ObjectOutputStream output = null;
        try {
            input = new ObjectInputStream(client.getInputStream());
            String serviceName = input.readUTF();
            String methodName = input.readUTF();
            Class<?>[] parameterTypes = (Class<?>[]) input.readObject();
            Object[] arguments = (Object[]) input.readObject();
            Class serviceClass = ServiceCenter.serviceRegistry.get(serviceName);
            if (serviceClass == null) {
                throw new ClassNotFoundException(serviceName + " not found");
            }
            Method method = serviceClass.getMethod(methodName, parameterTypes);
            Object result = method.invoke(serviceClass.newInstance(), arguments);

            output = new ObjectOutputStream(client.getOutputStream());
            output.writeObject(result);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (client != null) {
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
