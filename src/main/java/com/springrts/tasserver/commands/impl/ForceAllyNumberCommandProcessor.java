/*
	Copyright (c) 2010 Robin Vobruba <robin.vobruba@derisk.ch>

	This program is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.springrts.tasserver.commands.impl;


import com.springrts.tasserver.Account;
import com.springrts.tasserver.Battle;
import com.springrts.tasserver.Client;
import com.springrts.tasserver.Misc;
import com.springrts.tasserver.commands.AbstractCommandProcessor;
import com.springrts.tasserver.commands.CommandProcessingException;
import com.springrts.tasserver.commands.SupportedCommand;
import java.util.List;

/**
 * Sent by the founder of the battle when he is trying to force some other
 * clients ally number to 'allyno'.
 * The server will update clients battle status automatically.
 * @author hoijui
 */
@SupportedCommand("FORCEALLYNO")
public class ForceAllyNumberCommandProcessor extends AbstractCommandProcessor {

	public ForceAllyNumberCommandProcessor() {
		super(2, 2, Account.Access.NORMAL);
	}

	@Override
	public boolean process(Client client, List<String> args)
			throws CommandProcessingException
	{
		boolean checksOk = super.process(client, args);
		if (!checksOk) {
			return false;
		}

		if (client.getBattleID() == Battle.NO_BATTLE_ID) {
			return false;
		}
		Battle bat = getContext().getBattles().getBattleByID(client.getBattleID());
		if (bat == null) {
			return false;
		}
		if (bat.getFounder() != client) {
			return false; // only founder can force team/ally numbers
		}

		String username = args.get(0);
		String alllyTeamNumberStr = args.get(1);

		int alllyTeamNumber;
		try {
			alllyTeamNumber = Integer.parseInt(alllyTeamNumberStr);
		} catch (NumberFormatException e) {
			return false;
		}
		if ((alllyTeamNumber < 0) || (alllyTeamNumber > getContext().getEngine().getMaxTeams() - 1)) {
			return false;
		}

		Client target = getContext().getClients().getClient(username);
		if (target == null) {
			return false;
		}
		if (!bat.isClientInBattle(target)) {
			return false;
		}

		target.setBattleStatus(Misc.setAllyNoOfBattleStatus(target.getBattleStatus(), alllyTeamNumber));
		bat.notifyClientsOfBattleStatus(target);

		return true;
	}
}