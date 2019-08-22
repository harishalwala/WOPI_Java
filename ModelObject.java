import java.util.Date;

public class ModelObject {

	private String lockValue = "";
	private Date lockExpiryTime;
	private String accessToken;
	private long accessToken_ttl;
	private String fileId;
	private boolean locked;
	private String lockedBy;
	private String fileLength;

	public String getFileLength() {
		return fileLength;
	}

	public void setFileLength(String fileLength) {
		this.fileLength = fileLength;
	}

	public String getLockedBy() {
		return lockedBy;
	}

	public void setLockedBy(String lockedBy) {
		this.lockedBy = lockedBy;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public long getAccessToken_ttl() {
		return accessToken_ttl;
	}

	public void setAccessToken_ttl(long accessToken_ttl) {
		this.accessToken_ttl = accessToken_ttl;
	}

	public String getLockValue() {
		return lockValue;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public void setLockValue(String lockValue) {
		this.lockValue = lockValue;
	}

	public Date getLockExpiryTime() {
//		if (lockExpiryTime == null) {
//			lockExpiryTime = new Date();
//		}
		return lockExpiryTime;
	}

	public void setLockExpiryTime(Date lockExpiryTime) {
		this.lockExpiryTime = lockExpiryTime;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder("lockValue == " + lockValue + "lockExpiryTime ==" + lockExpiryTime);

		return builder.toString();
	}