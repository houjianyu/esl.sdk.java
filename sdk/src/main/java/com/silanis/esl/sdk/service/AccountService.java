package com.silanis.esl.sdk.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.silanis.esl.api.model.Result;
import com.silanis.esl.api.model.Sender;
import com.silanis.esl.api.util.JacksonUtil;
import com.silanis.esl.sdk.AccountMember;
import com.silanis.esl.sdk.EslException;
import com.silanis.esl.sdk.internal.*;
import com.silanis.esl.sdk.SenderInfo;
import com.silanis.esl.sdk.internal.RestClient;
import com.silanis.esl.sdk.internal.Serialization;
import com.silanis.esl.sdk.internal.UrlTemplate;
import com.silanis.esl.sdk.internal.converter.AccountMemberConverter;
import com.silanis.esl.sdk.internal.converter.SenderConverter;

import java.util.HashMap;
import java.util.Map;


/**
 * The AccountService provides methods to help create senders for an account
 */
public class AccountService {

    private UrlTemplate template;
    private RestClient client;

    public AccountService( RestClient client, String baseUrl ) {
        template = new UrlTemplate( baseUrl );
        this.client = client;
    }

    /**
     * Invite a new member to the account
     *
     * @param accountMember The member to be invited
     */
    public void inviteUser( AccountMember accountMember ) {
        String path = template.urlFor( UrlTemplate.ACCOUNT_MEMBER_PATH).build();
        Sender sender = new AccountMemberConverter( accountMember ).toAPISender();
        try {
            client.post( path, Serialization.toJson( sender ) );
        } catch (RequestException e){
            throw new EslServerException( "Unable to invite member to account.", e);
        } catch ( Exception e ) {
            throw new EslException( "Unable to invite member to account.", e );
        }
    }

    /**
     * Get a list of all the senders from the account
     *
     * @return A list mapping all the senders to their respective name
     */
    public Map<String, com.silanis.esl.sdk.Sender> getSenders(){
        String path = template.urlFor(UrlTemplate.ACCOUNT_MEMBER_PATH).build();

        try {
            String stringResponse = client.get(path);
            Result<Sender> apiResponse = JacksonUtil.deserialize(stringResponse, new TypeReference<Result<Sender>>() {
            });
            Map<String, com.silanis.esl.sdk.Sender> result = new HashMap<String, com.silanis.esl.sdk.Sender>();
            for ( Sender sender : apiResponse.getResults() ) {
                result.put(sender.getEmail(), new SenderConverter(sender).toSDKSender());
            }
            return result;
        }
        catch (RequestException e) {
            throw new EslServerException("Failed to retrieve Account Members List.", e);
        }
        catch (Exception e) {
            throw new EslException("Failed to retrieve Account Members List.", e);
        }
    }

    /**
     * Delete a sender from the account
     *
     * @param senderId The sender Id
     */

    public void deleteSender(String senderId){
        String path = template.urlFor(UrlTemplate.SENDER_PATH)
                .replace("{senderUid}", senderId)
                .build();
        try {
            client.delete( path );
        }
        catch ( RequestException e ) {
            throw new EslServerException( "Could not delete sender.", e );
        }
        catch ( Exception e ) {
            throw new EslException( "Could not delete sender." + " Exception: " + e.getMessage(), e );
        }
    }


    /**
     * Update the information of a sender
     *
     * @param sender The updated info of the sender
     * @param senderId The sender Id
     */
    public void updateSender(SenderInfo sender, String senderId){
        Sender apiPayload = new SenderConverter( sender ).toAPISender();
        apiPayload.setId(senderId);
        String path = template.urlFor(UrlTemplate.SENDER_PATH)
                .replace("{senderUid}", senderId)
                .build();
        try {
            String json = Serialization.toJson(apiPayload);
            client.post(path, json);
        }
        catch ( RequestException e ) {
            throw new EslServerException( "Could not update sender.", e );
        }
        catch ( Exception e ) {
            throw new EslException( "Could not update sender." + " Exception: " + e.getMessage(), e );
        }
    }
}
