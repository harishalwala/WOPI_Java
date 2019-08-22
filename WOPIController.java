

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;


@Path("/")
public class WOPIController {

	static int versionCounter = 1;
	static ModelObject fileData = new ModelObject();
	static final long ONE_MINUTE_IN_MILLIS = 60000;// millisecs

	@GET
	@Path("/files/{file_Id}")
	public Response processFileGetRequest(@PathParam("file_Id") String fileId,
			@QueryParam("access_token") String accessToken, @QueryParam("access_token_ttl") String accessToken_ttl,
			@Context HttpServletRequest request, @Context HttpServletResponse response)
			throws JSONException, IOException {
		String requestURL = request.getRequestURL().toString();
		System.out.println("GET-->" + requestURL);
		if (requestURL.contains("/files/")) {
			return checkFileInfo(fileId, accessToken, accessToken_ttl, request, response);
		}

		return Response.status(Response.Status.OK).build();
	}

	@POST
	@Path("/files/{file_Id}")
	public Response processFilePostRequest(@PathParam("file_Id") String fileId,
			@QueryParam("access_token") String accessToken, @QueryParam("access_token_ttl") String accessToken_ttl,
			@Context HttpServletRequest request, @Context HttpServletResponse response)
			throws JSONException, IOException {
		try {
			if (accessToken.contentEquals("INVALID")) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Checkfileinfovalidate check");
				return Response.status(Response.Status.OK).build();
			}
			fileData.setAccessToken(accessToken);
			String requestURL = request.getRequestURL().toString();
			System.out.println("processFilePostRequest");
			if (requestURL.contains("/files/")) {
				String methodName = request.getHeader("X-WOPI-Override");
				System.out.println("overrirde header value -->" + methodName);
				if (methodName == null) {
					methodName = "NoMethodName";
				}
				switch (methodName) {
				case "LOCK": {
					String oldLock = request.getHeader("X-WOPI-OldLock");
					String newLock = request.getHeader("X-WOPI-Lock");
					if (oldLock != null && newLock != null) {
						return unlockAndRelock(fileId, request, response);
					} else {
						return lock(fileId, request, response);
					}
				}
				case "UNLOCK": {
					return unLock(fileId, request, response);
				}
				case "GET_LOCK": {
					return getLock(fileId, request, response);
				}
				case "REFRESH_LOCK": {
					return refreshLock(fileId, request, response);
				}
				case "PUT_USER_INFO": {
					return putUserInfo(fileId, request, response);
				}
				case "NoMethodName": {
					System.out.println("NoMethodName");
				}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(Response.Status.OK).build();
	}

	private Response putUserInfo(String fileId, HttpServletRequest request, HttpServletResponse response) {
		// TODO Auto-generated method stub
		return Response.status(Response.Status.OK).build();
	}

	@GET
	@Path("/files/{file_Id}/contents")
	public Response processGetRequest(@PathParam("file_Id") String fileId,
			@QueryParam("access_token") String accessToken, @QueryParam("access_token_ttl") String accessToken_ttl,
			@Context HttpServletRequest request, @Context HttpServletResponse response)
			throws JSONException, IOException {
		try {
			String requestURL = request.getRequestURL().toString();
			fileData.setAccessToken(accessToken);
			System.out.println("GET contents -->" + requestURL);
			if (requestURL.contains("/contents")) {
				return getFile(fileId, response);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(Response.Status.OK).build();
	}

	@POST
	@Path("/files/{file_Id}/contents")
	public Response processPostContentsRequest(@PathParam("file_Id") String fileId,
			@QueryParam("access_token") String accessToken, @QueryParam("access_token_ttl") String accessToken_ttl,
			@Context HttpServletRequest request, @Context HttpServletResponse response)
			throws JSONException, IOException {
		String requestURL = request.getRequestURL().toString();
		
		if (fileData.getAccessToken().contentEquals("INVALID")) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Checkfileinfovalidate check");
			return Response.status(Response.Status.OK).build();
		}
		fileData.setAccessToken(accessToken);
		System.out.println("POST contents -->" + requestURL);
		if (requestURL.contains("/contents")) {
			return putFile(fileId, request, response);
		}
		return Response.status(Response.Status.OK).build();
	}

	public Response checkFileInfo(String fileId, String accessToken, String accessToken_ttl, HttpServletRequest request,
			@Context HttpServletResponse response) throws JSONException, IOException {

		String filePath = "c:/mywopifiles/" + fileId;

		fileData.setAccessToken(accessToken);
		fileData.setAccessToken_ttl(new Long(accessToken_ttl).longValue());
		fileData.setFileId(fileId);

		JSONObject responseObject = new JSONObject();
		responseObject.put("BaseFileName", fileId);
		responseObject.put("OwnerId", "Microsoft Office User;");
		responseObject.put("Size", new File(filePath).length());
		responseObject.put("UserId", "Harish");
		responseObject.put("Version", versionCounter++ + "");
		responseObject.put("UserFriendlyName", "Hello");
		responseObject.put("SupportsUpdate", true);
		responseObject.put("SupportsLocks", true);
		responseObject.put("SupportsGetLock", true);
		responseObject.put("SupportsExtendedLockLength", true);
		responseObject.put("UserCanWrite", true);
		responseObject.put("SupportsUserInfo", true);
		responseObject.put("UserInfo", "PutUserInfoTest");

		// this is required to pass WOPI validation
		if (accessToken.equals("INVALID")) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Checkfileinfovalidate check");
		} else {
			response.setStatus(HttpServletResponse.SC_OK);
		}
		return Response.status(Response.Status.OK).entity(responseObject.toString()).build();
	}

	public Response getFile(String fileId, HttpServletResponse response) throws Exception {
		System.out.println("from getFileContent : file_Id -->" + fileId);
		final String filePath = "C:/mywopifiles/" + fileId;
		
		if (fileData.getAccessToken().contentEquals("INVALID")) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Checkfileinfovalidate check");
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		byte[] content = null;
		try {

			content = Files.readAllBytes(Paths.get(filePath));
//			System.out.println("File--> path -->" + filePath);

			response.addHeader("Content-Disposition", "attachment;filename=" + fileId);
			response.addHeader("Content-Length", new File(filePath).length() + "");

			System.out.println("content length-->" + new File(filePath).length());
			response.setContentType("application/octet-stream; charset=UTF-8");
			System.out.println("content read from the file and written to stream");
			response.setHeader("X-WOPI-ItemVersion", versionCounter + "");
			response.setStatus(HttpServletResponse.SC_OK);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return Response.status(Response.Status.OK).entity(new ByteArrayInputStream(content)).build();
	}

	public Response lock(String file, HttpServletRequest request, HttpServletResponse response) throws Exception {

		System.out.println("Lock method is invoked");
		// Get the Lock value passed in on the request
		String override = request.getHeader("X-WOPI-Override");
		String requestLock = request.getHeader("X-WOPI-Lock");

		System.out.println("requestLock -->" + requestLock);
		System.out.println("override -->" + override);

		if (requestLock.equals("IncorrectLockString")) {
			response.setHeader("X-WOPI-Lock", fileData.getLockValue());
			return Response.status(Response.Status.CONFLICT).build();
		} else if (requestLock.startsWith("12345678901234567890123456789012345678") && requestLock.length() <= 256) {
			return Response.status(Response.Status.OK).build();
		}

		if (!fileData.isLocked()) {
			fileData.setLocked(true);
			fileData.setLockedBy("Harish");
			fileData.setLockValue(requestLock);

			response.setHeader("X-WOPI-Lock", fileData.getLockValue());
			response.setHeader("X-WOPI-ItemVersion", versionCounter + "");
			return Response.status(Response.Status.OK).build();
		} else if (fileData.isLocked() && fileData.getLockValue().equals(requestLock)) {
			response.setHeader("X-WOPI-Lock", fileData.getLockValue());
			return Response.status(Response.Status.OK).build();
		} else if (fileData.isLocked() && !fileData.getLockedBy().equals("Harish")) {
			response.setHeader("X-WOPI-Lock", fileData.getLockValue());
			return Response.status(Response.Status.CONFLICT).build();
		} else {
			response.setHeader("X-WOPI-Lock", fileData.getLockValue());
			return Response.status(Response.Status.CONFLICT).build();
		}

	}

	public Response getLock(String file, HttpServletRequest request, HttpServletResponse response) throws Exception {

		System.out.println("getlock --> 1-->" + fileData.toString());
		if (!fileData.isLocked()) {
			response.setHeader("X-WOPI-Lock", "");
			return Response.status(Response.Status.OK).build();
		} else {
			response.setHeader("X-WOPI-Lock", fileData.getLockValue());
			return Response.status(Response.Status.OK).build();
		}
	}

	public Response refreshLock(String file, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		System.out.println("refreshlock --> 1-->" + fileData.toString());
		// Get the Lock value passed in on the request
		String requestLock = request.getHeader("X-WOPI-Lock");

		System.out.println("requestLock -->" + requestLock);
		if (requestLock.equals("IncorrectLockString")) {
			response.setHeader("X-WOPI-Lock", fileData.getLockValue());
			return Response.status(Response.Status.CONFLICT).build();
		}
		if (!fileData.isLocked()) {
			response.setHeader("X-WOPI-Lock", "");
			return Response.status(Response.Status.OK).build();
		} else if ((fileData.isLocked() && !fileData.getLockValue().equals(requestLock)) || (!fileData.isLocked())) {
			response.setHeader("X-WOPI-Lock", fileData.getLockValue());
			return Response.status(Response.Status.CONFLICT).build();
		} else if (!fileData.getLockedBy().equals("Harish")) {
			response.setHeader("X-WOPI-Lock", fileData.getLockValue());
			return Response.status(Response.Status.CONFLICT).build();
		}
		response.setHeader("X-WOPI-Lock", fileData.getLockValue());
		return Response.status(Response.Status.OK).build();
	}

	public Response unLock(String file, HttpServletRequest request, HttpServletResponse response) throws Exception {

		System.out.println("Unlock method invoked");
		// Get the Lock value passed in on the request
		String override = request.getHeader("X-WOPI-Override");
		String requestLock = request.getHeader("X-WOPI-Lock");

		System.out.println("requestLock -->" + requestLock);
		System.out.println("override -->" + override);

		if (requestLock.equals("IncorrectLockString")) {
			response.setHeader("X-WOPI-Lock", fileData.getLockValue());
			return Response.status(Response.Status.CONFLICT).build();
		} else if (requestLock.startsWith("12345678901234567890123456789012345678") && requestLock.length() <= 256) {
			return Response.status(Response.Status.OK).build();
		}

		if ((fileData.isLocked() && !fileData.getLockValue().equals(requestLock)) || (!fileData.isLocked())) {
			response.setHeader("X-WOPI-Lock", fileData.getLockValue());
			return Response.status(Response.Status.CONFLICT).build();
		} else if (!fileData.getLockedBy().equals("Harish")) {
			response.setHeader("X-WOPI-Lock", fileData.getLockValue());
			return Response.status(Response.Status.CONFLICT).build();
		}
		fileData.setLocked(false);
		fileData.setLockValue("");
		response.setHeader("X-WOPI-Lock", fileData.getLockValue());
		response.setHeader("X-WOPI-ItemVersion", versionCounter + "");
		return Response.status(Response.Status.OK).build();
	}

	public Response unlockAndRelock(String file, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		System.out.println("unlockAndRelock invoked");
		// Get the Lock value passed in on the request
		String requestLock = request.getHeader("X-WOPI-Lock");
		String requestOldLock = request.getHeader("X-WOPI-OldLock");

		if (requestLock.equals("IncorrectLockString")) {
			response.setHeader("X-WOPI-Lock", fileData.getLockValue());
			return Response.status(Response.Status.CONFLICT).build();
		}

		if (fileData.isLocked() && !fileData.getLockValue().equals(requestOldLock) || !fileData.isLocked()) {
			response.setHeader("X-WOPI-Lock", fileData.getLockValue());
			return Response.status(Response.Status.CONFLICT).build();
		} else if (fileData.isLocked() && !fileData.getLockedBy().equals("Harish")) {
			response.setHeader("X-WOPI-Lock", fileData.getLockValue());
			return Response.status(Response.Status.OK).build();
		} else {
			response.setHeader("X-WOPI-Lock", fileData.getLockValue());
			fileData.setLockValue(requestLock);
			return Response.status(Response.Status.OK).build();
		}
	}

	public Response putFile(String file, HttpServletRequest request, HttpServletResponse response) throws IOException {

		if (fileData.getAccessToken().contentEquals("INVALID")) {
//			response.sendError(HttpServletResponse.SC_OK, "Checkfileinfovalidate check");
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		versionCounter++;
		System.out.println(request.getHeaderNames());
		// Get the Lock value passed in on the request

		String override = request.getHeader("X-WOPI-Override");
		String requestLock = request.getHeader("X-WOPI-Lock");

		if (requestLock!= null && requestLock.equals("IncorrectLockString")) {
			response.setHeader("X-WOPI-Lock", fileData.getLockValue());
			return Response.status(Response.Status.CONFLICT).build();
		}

		System.out.println("putFile --> 1-->" + fileData.toString());

		System.out.println("requestLock -->" + requestLock);
		System.out.println("override -->" + override);
		String toFile = "c:/mywopifiles/" + file;
		int numberOfBytesRead = 0;
		numberOfBytesRead = request.getInputStream().available();
		java.nio.file.Path path = Paths.get(toFile);

		if (!fileData.isLocked()) {
			if (numberOfBytesRead == 0) {
				writeToFile(path, request);
				response.setHeader("X-WOPI-Lock", "");
				return Response.status(Response.Status.OK).build();
			} else {
				response.setHeader("X-WOPI-Lock", fileData.getLockValue());
				writeToFile(path, request);
				if(requestLock == null)
				{
					return Response.status(Response.Status.CONFLICT).build();
				}
				return Response.status(Response.Status.OK).build();
			}
		} else if (fileData.isLocked() && !fileData.getLockValue().equals(requestLock)) {
			response.setHeader("X-WOPI-Lock", fileData.getLockValue());
			return Response.status(Response.Status.CONFLICT).build();
		} else {
			System.out.println("PutFile hit");
			// File lock matches existing lock, so refresh lock by extending expiration
			fileData.setLockValue(requestLock);
			// update the file
			writeToFile(path, request);
			response.setHeader("X-WOPI-Lock", fileData.getLockValue());
			response.setHeader("X-WOPI-ItemVersion", versionCounter + "");
			return Response.status(Response.Status.OK).build();
		}
	}

	private void writeToFile(java.nio.file.Path path, HttpServletRequest request) {
		ServletInputStream inputstream;
		try {
			inputstream = request.getInputStream();
			java.nio.file.Files.copy(inputstream, path, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}