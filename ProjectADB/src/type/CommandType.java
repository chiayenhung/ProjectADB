/**
 * 
 */
package type;

/**
 * @author ching-yingyang
 *
 */
public enum CommandType {
	beginReadOnly,
	begin,
	write,
	read,
	fail,
	dump,
	dumpSite,
	dumpX,
	recover,
	end,
	unkown,
}
